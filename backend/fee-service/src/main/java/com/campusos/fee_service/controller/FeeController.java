package com.campusos.fee_service.controller;

import com.campusos.fee_service.dto.request.FeeStructureRequest;
import com.campusos.fee_service.dto.request.GenerateFeesRequest;
import com.campusos.fee_service.dto.request.MarkPaidRequest;
import com.campusos.fee_service.dto.request.UpdateFeeStructureRequest;
import com.campusos.fee_service.dto.response.FeeStructureResponse;
import com.campusos.fee_service.dto.response.FeeSummaryResponse;
import com.campusos.fee_service.dto.response.StudentFeeResponse;
import com.campusos.fee_service.enums.FeeStatus;
import com.campusos.fee_service.enums.FeeType;
import com.campusos.fee_service.security.AuthenticatedUser;
import com.campusos.fee_service.service.FeeStructureService;
import com.campusos.fee_service.service.StudentFeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/fees")
@RequiredArgsConstructor
public class FeeController {

    private final FeeStructureService feeStructureService;
    private final StudentFeeService studentFeeService;

    // --- Fee structure (admin) ---

    @PostMapping("/structure")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FeeStructureResponse> createStructure(@Valid @RequestBody FeeStructureRequest request,
                                                               @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(feeStructureService.create(user.schoolId(), request));
    }

    @GetMapping("/structure")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FeeStructureResponse>> listStructures(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(feeStructureService.list(user.schoolId()));
    }

    @PutMapping("/structure/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FeeStructureResponse> updateStructure(@PathVariable UUID id,
                                                               @Valid @RequestBody UpdateFeeStructureRequest request,
                                                               @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(feeStructureService.update(user.schoolId(), id, request));
    }

    // --- Student fees (admin) ---

    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Integer>> generate(@Valid @RequestBody GenerateFeesRequest request,
                                                         @AuthenticationPrincipal AuthenticatedUser user) {
        int created = studentFeeService.generate(user.schoolId(), request);
        return ResponseEntity.ok(Map.of("created", created));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<StudentFeeResponse>> list(@RequestParam(required = false) String classLabel,
                                                         @RequestParam(required = false) FeeStatus status,
                                                         @RequestParam(required = false) FeeType feeType,
                                                         @PageableDefault(size = 20) Pageable pageable,
                                                         @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(studentFeeService.list(user.schoolId(), classLabel, status, feeType, pageable));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FeeSummaryResponse> summary(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(studentFeeService.summary(user.schoolId()));
    }

    @PutMapping("/{id}/mark-paid")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentFeeResponse> markPaid(@PathVariable UUID id,
                                                       @RequestBody(required = false) MarkPaidRequest request,
                                                       @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(studentFeeService.markPaid(user.schoolId(), user.userId(), id, request));
    }

    @PutMapping("/{id}/mark-pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentFeeResponse> markPending(@PathVariable UUID id,
                                                          @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(studentFeeService.markPending(user.schoolId(), user.userId(), id));
    }

    // --- Parent ---

    @GetMapping("/student/{id}")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<List<StudentFeeResponse>> studentFees(@PathVariable UUID id,
                                                               @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(studentFeeService.studentFees(user.userId(), id));
    }
}
