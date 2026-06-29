# Enterprise build — what's done vs what you do on your personal laptop

You wanted the backend enterprise-complete with **no rework**. Here's the honest split: I can't compile/run anything on this laptop, and the items below genuinely need a machine where you can build + run the infra (broker, MinIO, DB) to get right. So I locked the **architecture** (seams + contracts) so none of this requires redoing existing code — every item is **additive** or a **drop-in behind an existing interface**. Do them in the order listed.

## ✅ Already done here (rework-proof foundation)
- **Event contract** — `common-lib` `event/`: `EventTopology` (exchange `campusos.events`, routing keys) + payload records (`AbsentEvent`, `LeaveRequestedEvent`, `LeaveDecidedEvent`, `FeeEvent`, `HolidayPublishedEvent`, `AnnouncementPublishedEvent`, `PasswordResetEvent`). Publishers + consumer share these — no JSON drift.
- **RS256 JWT** — RSA keypair generated (`auth-service`/`api-gateway` `resources/keys/`). `common-lib` `security/RsaKeys`. auth signs with the private key; gateway + auth verify with the public key. HS256 secret removed. Override key paths in prod via `JWT_PRIVATE_KEY_LOCATION` / `JWT_PUBLIC_KEY_LOCATION`.
- **Seams in place:** `EmailSender` (messaging) → SMTP is a drop-in; `FileStorage` (school) → MinIO is a drop-in. Cross-school report Feign already wired.

Everything below is implemented where you can verify it. None of it changes the public API or the data model, so it won't cascade into rework.

---

## 1. RabbitMQ event bus  (replaces the best-effort REST notify)
**Why on your laptop:** AMQP wiring (converter on both ends, queue/binding declaration, typed listeners) is best verified against a running broker.
**No rework:** the event payloads + `EventTopology` are already in `common-lib`; this is "publish instead of REST-call" + "listen instead of REST-controller."

**Deps** (add `spring-boot-starter-amqp` to `auth`, `academic`, `fee`, `calendar`, `message`):
```xml
<dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-amqp</artifactId></dependency>
```
**Shared config** (every rabbit service) — one Jackson converter bean so payloads travel as JSON:
```java
@Configuration
class RabbitConfig {
  @Bean MessageConverter jsonConverter() { return new Jackson2JsonMessageConverter(); }
}
```
**Publishers** — replace each `notificationClient.x(...)` call with:
```java
rabbitTemplate.convertAndSend(EventTopology.EXCHANGE, EventTopology.ATTENDANCE_ABSENT, new AbsentEvent(...));
```
Map: auth `forgotPassword` → `PASSWORD_RESET`/`PasswordResetEvent`; academic absentees → `ATTENDANCE_ABSENT`, leave apply/decide → `LEAVE_REQUESTED`/`LEAVE_DECIDED`; fee mark-paid → `FEE_STATUS_CHANGED` (kind RECEIPT), reminder cron → `FEE_REMINDER`; calendar → `HOLIDAY_PUBLISHED`/`ANNOUNCEMENT_PUBLISHED`. Declare the exchange once: `@Bean TopicExchange ex(){return new TopicExchange(EventTopology.EXCHANGE,true,false);}`
**Consumer (message-service)** — declare a queue per event bound to its key, and a typed listener each:
```java
@Bean Queue absentQ(){ return QueueBuilder.durable("messaging.absent").build(); }
@Bean Binding b1(Queue absentQ, TopicExchange ex){ return BindingBuilder.bind(absentQ).to(ex).with(EventTopology.ATTENDANCE_ABSENT); }
@RabbitListener(queues="messaging.absent")
public void onAbsent(AbsentEvent e){ notificationService.handleAbsentees(List.of(e)); }   // reuse existing handlers
```
**Remove after wiring:** the `NotificationClient` Feign interfaces (academic/fee/calendar/auth) + their `client/dto/*Notification` records, and `message-service`'s `NotificationInternalController` (the REST `/api/internal/notifications/*`). Keep `NotificationService` handlers — just call them from the listeners (swap the message-service event DTOs for the `common-lib` ones).
**Gotcha:** enable retry/DLQ via `spring.rabbitmq.listener.simple.retry.enabled=true` and a dead-letter queue so a bad message doesn't hot-loop.

## 2. MinIO object storage  (drop-in behind `FileStorage`)
**No rework:** `FileStorage` interface already exists; `FileController` depends on it. Just add a second impl and mark it `@Primary`.
**Deps** (school-service + calendar-service):
```xml
<dependency><groupId>io.minio</groupId><artifactId>minio</artifactId><version>8.5.7</version></dependency>
```
**Config:** `app.minio.endpoint`, `access-key`, `secret-key`, `bucket` (env).
**Code:** `@Component @Primary class MinioFileStorage implements FileStorage` — `MinioClient.builder().endpoint(...).credentials(...).build()`; on startup ensure bucket (`bucketExists`/`makeBucket`); `store` → `putObject(PutObjectArgs...)`; gallery download → `getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().method(Method.GET)...)`. For calendar, swap the gallery stub URL for a presigned URL.
**Gotcha:** verify the 8.5.x builder method names when you compile (the SDK is builder-based).

## 3. SMTP email  (drop-in behind `EmailSender`)
**No rework:** `EmailSender` interface exists; `LoggingEmailSender` is the fallback.
**Deps** (message-service): `spring-boot-starter-mail`.
**Config:** `spring.mail.host/port/username/password` (dev: MailHog `host=mailhog port=1025`, no auth).
**Code:** `@Component @Primary class SmtpEmailSender implements EmailSender` using `JavaMailSender` (`SimpleMailMessage`). Keep `LoggingEmailSender` for the `default`/test profile so tests don't need SMTP.

## 4. Flyway migrations  ⚠ do this the SAFE way (don't hand-write DDL)
**Why:** with `ddl-auto: validate`, hand-written DDL must match Hibernate's mapping exactly (UUID→`BINARY(16)`, enums→`VARCHAR`, `Boolean`→`BIT`) — getting it wrong by hand is the #1 rework trap.
**Safe procedure per service:**
1. Keep `ddl-auto: update`, start the service once against an empty DB → Hibernate creates the correct schema.
2. Export it: `mysqldump --no-data --skip-comments <db> > V1__init.sql` (or `SHOW CREATE TABLE`).
3. Put it at `src/main/resources/db/migration/V1__init_<svc>.sql`.
4. Add `flyway-core` + `flyway-mysql`; set `spring.flyway.enabled=true`, `baseline-on-migrate=true`; switch `ddl-auto: validate`.
5. Restart → Flyway applies V1, Hibernate validates against the schema *it generated* → guaranteed match.
This gives versioned migrations with **zero** DDL guesswork.

## 5. Redis — cache + rate limit
**Cache (safe, drop-in):** `spring-boot-starter-data-redis` + `@EnableCaching`; `@Cacheable` on hot reads — auth `RoleServiceImpl.getByName`, school `getSchoolSummary` / class-teacher lookups. Config `spring.data.redis.host`. Set `@CacheEvict` on the matching writes.
**Rate limit (gateway, the fiddly bit):** the WebMVC gateway has no built-in `RequestRateLimiter`. Use **`bucket4j-spring-boot-starter`** (Redis-backed) or a small `OncePerRequestFilter` doing a Redis `INCR`+`EXPIRE` fixed-window per `X-Auth-User-Id`/IP. Keep it a separate filter ordered after `AuthGatewayFilter`.

## 6. Resilience4j  (additive; existing try/catch already degrades)
**Deps** (services with Feign): `spring-cloud-starter-circuitbreaker-resilience4j`.
**Config:** `spring.cloud.openfeign.circuitbreaker.enabled=true` + resilience4j instance defaults (timeouts already set per-client in yaml). On an open circuit the call throws and your existing `catch` turns it into a 503/graceful-degrade — so **no behavior change to reconcile**. Fallback factories are optional.

## 7. Observability  (config-only)
**Deps** (all services): `micrometer-tracing-bridge-brave`, `io.zipkin.reporter2:zipkin-reporter-brave`, `spring-boot-starter-actuator` (already present most places).
**Config:** `management.tracing.sampling.probability=1.0` (dev), `management.zipkin.tracing.endpoint=http://zipkin:9411/api/v2/spans`, and a log pattern with `[%X{traceId},%X{spanId}]`. Expose `management.endpoints.web.exposure.include=health,info,prometheus`.

## 8. Docker / Compose  (can't run here — files to create on your laptop)
**Per service `Dockerfile`** (multi-stage):
```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn -q -pl <service> -am -DskipTests package
FROM eclipse-temurin:21-jre
COPY --from=build /app/<service>/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
```
**`docker-compose.yml`** services: `mysql:8`, `rabbitmq:3-management`, `minio/minio`, `redis:7`, `mailhog/mailhog`, `openzipkin/zipkin`, then `service-registry`, `config-server`, `auth/school/academic/calendar/fee/message`, `api-gateway` — each with env for the infra hostnames (`SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/...`, `SPRING_RABBITMQ_HOST=rabbitmq`, `SPRING_DATA_REDIS_HOST=redis`, `app.minio.endpoint=http://minio:9000`, `EUREKA...=http://service-registry:8761/eureka`). `depends_on` infra.
**`init-databases.sql`** (mounted to mysql `/docker-entrypoint-initdb.d/`):
```sql
CREATE DATABASE IF NOT EXISTS auth_db;      CREATE DATABASE IF NOT EXISTS school_db;
CREATE DATABASE IF NOT EXISTS academic_db;  CREATE DATABASE IF NOT EXISTS calendar_db;
CREATE DATABASE IF NOT EXISTS fee_db;       CREATE DATABASE IF NOT EXISTS messaging_db;
```
**`.env.example`** — DB creds, MinIO keys, SMTP creds, JWT key paths.
> Tell me if you'd like me to **write all the Dockerfiles + the full compose + init SQL + .env.example now** — those are just declarative text files (no compile risk), so I can do them here and you'd only run them. I left them off pending your go-ahead since you said Docker is laptop-side.

---

## Order to implement
1. Flyway (safe procedure) → stable schema first.
2. RabbitMQ → the core async backbone.
3. MinIO + SMTP → drop-ins behind the seams.
4. Redis cache → Resilience4j → Observability (additive layers).
5. Docker Compose → ties it all together for one-command run.

## What stays env/secret (never in code)
Real DB password, MinIO keys, SMTP creds, and the **production RSA keypair** (replace the committed dev keys via `JWT_*_KEY_LOCATION`).
