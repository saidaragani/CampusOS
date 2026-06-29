# CampusOS — Backend Project Overview

A multi-tenant **school management platform** (SaaS). One deployment serves many schools; every record is isolated by `schoolId`. Built as Spring Boot microservices behind an API gateway, with per-service databases and JWT-based auth.

> **Status:** All services are implemented in code. They have **not been compiled/run in this environment** (the office laptop can't download Maven dependencies) — build & run on a machine with internet. Tests are written (H2 context smoke tests + unit tests) but unrun.

---

## 1. The product arc (what the system does)

1. **Super admin creates a school** (name, code e.g. `GVS`, address) and the school's **one admin** account.
2. **Admin adds teachers** — each teacher gets a domain record (with admin-only salary) **and** a TEACHER login.
3. **Admin assigns each teacher a class label** (`6-A`, `7-B`…). One class teacher per label.
4. **Admin admits students** — auto-generated admission number (`GVS-2025-0001`), class label stored on the student.
5. **Class connects automatically** — everything keys off `(schoolId, classLabel)`; the teacher of `6-A` owns every `6-A` student.
6. **Parent self-registers** with the admission number (verified, first-come-wins uniqueness).
7. **Daily use** — attendance (AM/PM), behaviour ratings, homework/reminders, leave requests, fees, calendar, gallery, announcements.
8. **Automated emails** — absent → parent, fee reminders, holidays/announcements, birthday & festival wishes.

---

## 2. Tech stack

| Concern | Choice |
|---|---|
| Language / build | Java 21, Maven (multi-module) |
| Framework | Spring Boot 3.5.16, Spring Cloud 2025.0.3 |
| Persistence | Spring Data JPA + MySQL (one DB per service) |
| Service discovery | Netflix Eureka |
| Config | Spring Cloud Config (optional import) |
| Gateway | Spring Cloud Gateway **Server WebMVC** (servlet) |
| Inter-service calls | OpenFeign |
| Auth | JWT (jjwt 0.12.7, HS256), BCrypt |
| Mapping/util | Lombok; plain static mappers (no MapStruct) |
| IDs | **UUID everywhere** (entities + JWT claims + contracts) |

---

## 3. Module map

| Module | Port | Eureka name | Database | Role |
|---|---|---|---|---|
| `config-server` | 8888 | config-server | — | Central config (infra) |
| `service-registry` | 8761 | service-registry | — | Eureka discovery (infra) |
| `common-lib` | — | — | — | Shared base classes + contracts |
| `api-gateway` | 8080 | api-gateway | — | Routing + edge JWT validation |
| `auth-service` | 8081 | auth-service | `auth_db` | Identity, accounts, JWT, parent links |
| `user-service` | 8082 | user-service | — | **Skipped** (redundant; auth owns users) |
| `academic-service` | 8083 | academic-service | `academic_db` | Attendance, ratings, timetable, leave, class-updates |
| `calender-service`¹ | 8084 | calendar-service | `calendar_db` | Holidays, events, gallery, announcements |
| `fee-service` | 8085 | fee-service | `fee_db` | Fee structure, student fees |
| `message-service` | 8086 | **message-service** | `messaging_db` | Email notifications (event + scheduled) |
| `school-service`² | 8087 | school-service | `school_db` | Schools, admins, teachers, classes, students |

¹ Directory is misspelled `calender-service`; Java package is `com.campusos.calendar_service`, Eureka name `calendar-service`.
² Directory `school-service`; package `com.campusos.school_service` (the old `tenant_service` scaffold was removed).

---

## 4. Architecture & conventions

### 4.1 Request flow
```
Client → api-gateway (8080) → validates JWT → injects X-Auth-* headers → routes (lb://) → service
                                                                          │
                                            service trusts headers → builds SecurityContext (ROLE_*)
```

### 4.2 Security model
- **auth-service issues JWTs** (HS256). Login returns an access token with claims: `sub`=email, `userId`, `schoolId` (null for super admin), `role`, `fullName`; plus a **DB-backed, rotating, revocable refresh token**. The access token itself is stateless and **cannot be revoked before expiry** (logout kills only the refresh token) — keep access-token TTL short.
- **api-gateway validates the JWT for all gateway-routed traffic**, then forwards trusted identity headers and **strips any client-supplied copies** (anti-spoofing): `X-Auth-User-Id`, `X-Auth-Email`, `X-Auth-Role`, `X-Auth-School-Id`. The original `Authorization` header is passed through unchanged.
- **auth-service also validates the JWT itself** (its own `JwtAuthenticationFilter`). This is what lets school→auth provisioning calls (below) authenticate even though they bypass the gateway. The HS256 secret therefore lives **only in api-gateway + auth-service**; all other services hold no secret.
- **Downstream services trust the headers** via a `GatewayHeaderAuthFilter` that builds a Spring `SecurityContext` with authority `ROLE_<role>` and an `AuthenticatedUser` principal. Role rules enforced with `@PreAuthorize`.
- **`/api/internal/**` endpoints are service-to-service only** — not routed by the gateway, `permitAll` inside each service. Used by Feign clients.
- **school-service → auth-service** forwards the caller's `Authorization` header (so auth's own JWT filter + `@PreAuthorize` see the real SUPER_ADMIN/ADMIN when provisioning logins).

> **⚠ Hard deployment requirement.** Because services build their security context from `X-Auth-*` headers (not from the JWT), a caller who can reach a service **directly** (bypassing the gateway) could forge `X-Auth-Role: SUPER_ADMIN`. Therefore ports **8081–8087 and all `/api/internal/**` endpoints MUST be unreachable from outside the internal network** (firewall / container network / no public binding). **Only the gateway (8080) is exposed publicly.** This is a security requirement, not a nice-to-have.

### 4.3 Multi-tenancy
Every domain table carries `school_id`; every query is scoped to the caller's school (from the header). The class "join" is purely `(schoolId, classLabel)` — there is **no separate `Class` entity**. The `class_teacher` row *is* the class: it maps `(schoolId, classLabel) → teacherId`, and its UUID is what the `/api/classes/{id}` endpoints address. So a class label is the source of truth for membership; `class_teacher` just records who teaches it.

### 4.4 Resilience
Cross-service reads degrade gracefully; writes that need a sibling reject cleanly with **503** rather than silently succeeding. Outbound emails/notifications are **best-effort** (failures logged, never block the originating action).

### 4.5 Package convention (every service)
```
controller/        REST controllers
service/           interfaces
serviceimpl/       @Service implementations
repository/        Spring Data repositories
entity/            JPA entities (extend common-lib BaseEntity)
dto/request, dto/response
client/  (+ client/dto)   Feign clients
security/          AuthHeaders, AuthenticatedUser, GatewayHeaderAuthFilter, SecurityConfig
exception/         ApiException + subtypes + GlobalExceptionHandler
enums/             domain enums
```

---

## 5. Shared contracts (`common-lib`)

Base: `BaseEntity` (auditing `createdAt`/`updatedAt`), `ApiResponse`, `ErrorCode`, `BusinessException`, `@StrongPassword`.

Inter-service contracts (`com.campusos.common_lib.contract`):
| Contract | Produced by | Consumed by |
|---|---|---|
| `SchoolSummary` (id, name, code, logoUrl) | school | auth (parent header) |
| `StudentSummary` (studentId, schoolId, admissionNo, name, classLabel, section, classTeacherName) | school | auth, academic |
| `RosterStudent` (…, guardianPhone, **hasBus**) | school | academic, fee, messaging |
| `TeacherClassView` (teacherId, schoolId, classLabel) | school | academic |
| `ChildLink` (studentId, schoolId, admissionNo) | auth | academic, fee |
| `RecipientContact` (email, fullName) | auth, school | messaging |
| `PasswordResetNotification` (email, fullName, resetToken) | auth | messaging |

*`StudentSummary.section` is **derived** from `classLabel` (e.g. `6-A` → `A`) at mapping time, not stored separately — `classLabel` is the single source of truth.*

---

## 6. Service details

### 6.1 auth-service (`auth_db`)
**Entities:** `User` (UUID id, email, password_hash, fullName, phone, `@ManyToOne Role`, schoolId, teacherId, parentId, enabled, lastLoginAt), `Role` (table, enum `RoleType` = SUPER_ADMIN/ADMIN/TEACHER/PARENT), `RefreshToken`, `PasswordResetToken`, `ParentStudentLink` (unique `(school_id, admission_no)`).
**Bootstrap:** seeds the 4 roles + one SUPER_ADMIN from config on startup (idempotent).
**Public endpoints** `/api/auth`:
- `POST /login`, `POST /refresh`, `POST /logout`
- `POST /register/parent` (verifies admission via school-service, first-come uniqueness)
- `POST /forgot-password`, `POST /reset-password`
**Authenticated:**
- `POST /change-password`, `GET /me`
- `GET /me/context` [P] — user + school header (name/logo) + children (class/teacher), enriched best-effort
- `POST /children/link` [P], `GET /children` [P]
- `POST /admins` [SUPER_ADMIN] — create a school's ADMIN login
- `POST /teachers` [ADMIN] — create a TEACHER login bound to caller's school
**Internal:** `/api/internal/parents/{userId}/children`, `/students/{studentId}/parent-recipients`, `/schools/{schoolId}/parent-recipients`.

### 6.2 school-service (`school_db`)
**Entities:** `School` (code unique = admission prefix), `SchoolAdmin` (one per school), `Teacher` (salary admin-only), `ClassTeacher` (`(school,classLabel)` unique), `Student` (auto admission no.), `AdmissionSequence` (per school+year, pessimistic-locked).
**Endpoints:**
- Schools [SA]: `POST/GET /api/schools`, `GET/PUT /{id}`, `POST /{id}/logo`, `POST/GET /{id}/admin`, `GET /overview` (counts), `GET /reports` (counts + attendance-% + fee-collection-% per school, enriched best-effort from academic + fee services)
- Files [SA/A/T]: `POST /api/files` (multipart upload → `{key,url}`), `GET /api/files/{folder}/{filename}` [authenticated] (download). Local-disk backed (`app.storage.base-dir`); the upload returns a URL you then set on `student.photoUrl` / `school.logoUrl`. Swap point for MinIO later.
- Classes [A]: `POST/GET /api/classes`, `GET/PUT/DELETE /{id}`, `PUT /{id}/teacher` — these operate on `class_teacher` rows (`{id}` = class_teacher UUID); there is no separate Class table.
- Teachers [A]: `POST/GET /api/teachers`, `GET/PUT /{id}`, `PUT /{id}/deactivate`; [T] `GET /me/class`, `GET /me/roster`
- Students [A]: `POST/GET /api/students`, `GET /{id}` [A/T], `PUT /{id}`, `PUT /{id}/deactivate`, `POST /{id}/photo`
**Internal:** `students/lookup`, `students/roster`, `students/by-class`, `students/birthdays`, `schools/{id}`, `teachers/{id}`, `teachers/by-user/{userId}`, `schools/{id}/teacher-recipients`.
**Provisioning:** creating an admin/teacher calls auth-service (Feign, JWT forwarded) to create the login, then stores the domain record with the returned `userId`.

### 6.3 academic-service (`academic_db`)
**Entities:** `Attendance` (unique `(student,date,session)`), `BehaviourRating` (unique `(student,month)`), `TimetableSlot`, `LeaveRequest`, `ClassUpdate`.
**Endpoints:**
- Attendance: `POST /api/attendance` [T] bulk-mark (upsert; absentees → messaging best-effort), `PUT /{id}` [T] correct, `GET /api/attendance` [T] day sheet, `GET /today` [A], `GET /summary` [A], `GET /student/{id}` [P]
- Ratings: `POST /api/ratings` [T], `GET /api/ratings` [T] (class+month), `GET /student/{id}` [P/A]
- Timetable: `POST /api/timetable` [A] replace, `GET /class/{classLabel}` [A/T/P], `GET /me` [T]
- Leaves: `POST /api/leaves` [P] apply, `GET /student/{id}` [P], `GET /pending` [T], `PUT /{id}/decide` [T]
- Class updates: `POST /api/class-updates` [T], `GET /class/{classLabel}` [T/P], `DELETE /{id}` [T]
**Dependencies:** resolves the teacher's class + roster from school-service; parent ownership from auth-service; absentee emails to messaging.
**Internal:** `GET /api/internal/attendance/school-stats?schoolId=&from=&to=` (school-wide attendance %, for cross-school reports).

### 6.4 calendar-service (`calendar_db`)
**Entities:** `Holiday`, `SchoolEvent` (type SPORTS/CULTURAL/EXAM/OTHER), `GalleryItem` (stores object key + metadata), `Announcement` (audience ALL_PARENTS/CLASS/TEACHERS).
**Endpoints:** `/api/holidays` [A CRUD, A/T/P list], `/api/events` [A/T CRUD, A/T/P list], `/api/gallery` (upload metadata, browse, `GET /{id}/url` stub presigned URL, delete), `/api/announcements` (post/list/update/delete; list filtered by role + class).
**Notifications:** publishes holiday & announcement to messaging (best-effort).
**Stub:** gallery stores keys + returns a stub URL — **no MinIO** (drop-in later).

### 6.5 fee-service (`fee_db`)
**Entities:** `FeeStructure` (per school+year+class, `class_label` null = all classes), `StudentFee` (unique `(student,year,feeType)`; `class_label` added for filtering).
**Endpoints:** `POST/GET /api/fees/structure`, `PUT /structure/{id}`, `POST /api/fees/generate` (pulls roster → SCHOOL fee for all + BUS fee for bus students), `GET /api/fees` (filter classLabel/status/feeType), `GET /summary`, `PUT /{id}/mark-paid`, `PUT /{id}/mark-pending`, `GET /student/{id}` [P].
**Scheduled:** daily overdue-pending scan → fee reminders (best-effort).
**Notifications:** receipt on mark-paid, reminders via cron.
**Internal:** `GET /api/internal/fees/school-stats?schoolId=` (collection %, for cross-school reports).

### 6.6 message-service (`messaging_db`, app name `message-service`)
**Entities:** `NotificationLog` (every send recorded SENT/FAILED), `WishSchedule` (festival wishes).
**Internal (consumed best-effort):** `POST /api/internal/notifications/{absentees, fees, holiday, announcement}`.
**Admin** `/api/notifications`: `POST/GET /festival`, `DELETE /festival/{id}`, `GET /log` (paged, filter by status), `POST /{id}/retry`.
**Scheduled:** daily birthday scan + festival-due scan.
**Email:** `EmailSender` interface with a `LoggingEmailSender` impl — **no SMTP/RabbitMQ**; resolves recipients via auth (parent emails) + school (teacher emails, birthdays). Lightweight REST + `@Scheduled` instead of a broker (per 10-school decision).

---

## 7. Build & run

**Build all** (on a machine with internet):
```bash
mvn -f "backend/pom.xml" clean install
```
**Start order:**
1. `service-registry` (8761), MySQL server
2. `config-server` (8888) — optional
3. `auth-service`, `school-service`, `academic-service`, `calendar-service`, `fee-service`, `message-service`
4. `api-gateway` (8080)

All traffic goes through `http://localhost:8080`. Databases auto-create (`createDatabaseIfNotExist=true`) and schema is managed by Hibernate **`ddl-auto: update`** — fine for dev; for production switch to **Flyway migrations with `ddl-auto: validate`** (update doesn't drop/rename cleanly and can produce surprising column types). Super-admin seeded from `app.super-admin.*` (default `superadmin@campusos.com` / `ChangeMe@123`).

**Run tests:** `mvn test` (uses in-memory H2 profile; no MySQL/Eureka needed).

---

## 8. End-to-end smoke test (the arc, via the gateway)
1. `POST /api/auth/login` as super admin → access token.
2. `POST /api/schools` (SA) → school; `POST /api/schools/{id}/admin` (SA) → admin login.
3. Login as admin → `POST /api/teachers`, `POST /api/classes`, `PUT /api/classes/{id}/teacher`, `POST /api/students` (note the admission number).
4. `POST /api/auth/register/parent` with that admission number → parent.
5. Login as teacher → `GET /api/teachers/me/roster`, `POST /api/attendance`.
6. Login as parent → `GET /api/auth/me/context`, `GET /api/attendance/student/{id}`, `GET /api/fees/student/{id}`.

---

## 9. Deliberate stubs & future swaps (10-school scope)
| Area | Now | Swap when scaling |
|---|---|---|
| Photo/logo storage | **local disk** (`/api/files`, `app.storage.base-dir`) | MinIO/S3 behind the same `LocalFileStorage` contract |
| Gallery storage | object keys + stub URLs | MinIO client + presigned URLs |
| Messaging transport | REST `/api/internal/...` + `@Scheduled` | RabbitMQ consumers + cron |
| Email | `LoggingEmailSender` | SMTP `JavaMailSender` |
| JWT | HS256 shared secret | RS256/JWKS (services verify with public key) |
| Caching / rate limit | none | Redis |

---

## 10. Build & review notes

**Verified during code review (clean — no action needed):**
- Gateway uses `spring-cloud-starter-gateway-server-webmvc` (servlet), not the reactive starter — so `spring.cloud.gateway.server.webmvc.routes` (correct prefix for 2025.0.x) is read by the right runtime.
- Every gateway `lb://` target and every `@FeignClient(name=...)` matches a registered `spring.application.name` (no Eureka-name mismatch).
- All parent `/student/{id}` routes (attendance, ratings, leaves, fees) enforce a `requireOwnedChild` ownership check — no IDOR.
- The HS256 secret is present only in api-gateway + auth-service.

**Fixes applied after review:**
- **Admission-number cold-start race** — the first admission of a school+year now seeds the sequence row in a separate (`REQUIRES_NEW`) transaction before the pessimistic lock, so concurrent first-admissions can't collide on the unique key (`AdmissionSequenceInitializer`).
- **Slow-notify coupling** — academic/fee/calendar now set short Feign timeouts (2s/3s) on the `message-service` client, so a *slow* (not just down) messaging service fails fast and is swallowed by the best-effort catch instead of stalling the originating action.

**Still verify on first real build (couldn't run here):**
1. `mvn clean install` actually compiles (office laptop can't fetch deps). Also confirm the exact patch `spring-cloud 2025.0.3` resolves; bump to the latest `2025.0.x` if the BOM complains.
2. Confirm `X-Auth-*` custom headers forward through the MVC gateway to a downstream service. (They're custom headers, not `X-Forwarded-*`, so 2025.0.0's default-off `X-Forwarded`/`trusted-proxies` handling shouldn't affect them — but it's a known source of "headers mysteriously missing" confusion.)

---

## 11. Not built / out of scope
- **user-service** — redundant; auth-service owns all user accounts.
- **Token strategy decision:** kept the **DB-backed refresh-token flow** (`/refresh` + revocable `/logout`) rather than the original "no refresh" plan — strictly better. Access token TTL is 1h (`jwt.access-token-expiration`, configurable); the access token itself is stateless (not revocable before expiry).
- Real file storage (MinIO), message broker (RabbitMQ), SMTP, Redis, config-repo contents.
- Frontend (this is backend only).
