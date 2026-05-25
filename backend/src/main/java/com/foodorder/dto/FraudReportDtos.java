package com.foodorder.dto;

import com.foodorder.enums.FraudReportStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class FraudReportDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateFraudReportRequest {
        @NotNull
        private Long reportedUserId;
        @NotBlank
        @Size(max = 1000)
        private String reason;
        private Long relatedOrderId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateFraudReportStatusRequest {
        @NotNull
        private FraudReportStatus status;
        private String adminNotes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FraudReportResponse {
        private Long id;
        private Long reportedUserId;
        private String reportedUsername;
        private Long reportingManagerId;
        private String reportingManagerUsername;
        private Long restaurantId;
        private String restaurantName;
        private Long relatedOrderId;
        private String reason;
        private String adminNotes;
        private FraudReportStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime resolvedAt;
    }
}
