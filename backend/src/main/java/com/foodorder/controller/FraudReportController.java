package com.foodorder.controller;

import com.foodorder.dto.ApiResponse;
import com.foodorder.dto.FraudReportDtos.*;
import com.foodorder.security.UserPrincipal;
import com.foodorder.service.FraudReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fraud-reports")
@CrossOrigin
@Tag(name = "Fraud Reports", description = "Managers report suspicious users; admins review and act")
@SecurityRequirement(name = "bearerAuth")
public class FraudReportController {

    private final FraudReportService reportService;

    public FraudReportController(FraudReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(summary = "Manager files a fraud report against a user")
    @PostMapping
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<FraudReportResponse> create(@Valid @RequestBody CreateFraudReportRequest req,
                                                      @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(reportService.create(principal.getUsername(), req));
    }

    @Operation(summary = "Manager lists their own filed reports")
    @GetMapping("/mine")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<FraudReportResponse>> mine(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(reportService.listMine(principal.getUsername()));
    }

    @Operation(summary = "Admin lists all fraud reports")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FraudReportResponse>> listAll() {
        return ResponseEntity.ok(reportService.listAll());
    }

    @Operation(summary = "Admin lists open (unresolved) reports")
    @GetMapping("/open")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FraudReportResponse>> listOpen() {
        return ResponseEntity.ok(reportService.listOpen());
    }

    @Operation(summary = "Admin marks a report RESOLVED or DISMISSED")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FraudReportResponse> updateStatus(@PathVariable Long id,
                                                            @Valid @RequestBody UpdateFraudReportStatusRequest req) {
        return ResponseEntity.ok(reportService.updateStatus(id, req));
    }

    @Operation(summary = "Admin deletes a fraud report")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id) {
        reportService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Report deleted"));
    }
}
