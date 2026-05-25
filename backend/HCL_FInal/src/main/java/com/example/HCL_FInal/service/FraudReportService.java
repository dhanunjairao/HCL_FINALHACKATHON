package com.example.HCL_FInal.service;

import com.example.HCL_FInal.dto.FraudReportDtos.*;
import com.example.HCL_FInal.entity.FraudReport;
import com.example.HCL_FInal.entity.Restaurant;
import com.example.HCL_FInal.entity.User;
import com.example.HCL_FInal.enums.FraudReportStatus;
import com.example.HCL_FInal.enums.Role;
import com.example.HCL_FInal.exception.BadRequestException;
import com.example.HCL_FInal.exception.ResourceNotFoundException;
import com.example.HCL_FInal.repository.FraudReportRepository;
import com.example.HCL_FInal.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FraudReportService {

    private final FraudReportRepository reportRepository;
    private final UserRepository userRepository;

    public FraudReportService(FraudReportRepository reportRepository, UserRepository userRepository) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
    }

    public FraudReportResponse create(String managerUsername, CreateFraudReportRequest req) {
        User manager = userRepository.findByUsername(managerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
        if (manager.getRole() != Role.MANAGER) {
            throw new BadRequestException("Only managers can report fraud");
        }
        User reported = userRepository.findById(req.getReportedUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Reported user not found: " + req.getReportedUserId()));
        if (reported.getId().equals(manager.getId())) {
            throw new BadRequestException("You cannot report yourself");
        }

        FraudReport report = FraudReport.builder()
                .reportedUser(reported)
                .reportingManager(manager)
                .restaurant(manager.getManagedRestaurant())
                .relatedOrderId(req.getRelatedOrderId())
                .reason(req.getReason())
                .status(FraudReportStatus.OPEN)
                .build();
        return toResponse(reportRepository.save(report));
    }

    public List<FraudReportResponse> listAll() {
        return reportRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<FraudReportResponse> listOpen() {
        return reportRepository.findByStatusOrderByCreatedAtDesc(FraudReportStatus.OPEN).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<FraudReportResponse> listMine(String managerUsername) {
        User manager = userRepository.findByUsername(managerUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found"));
        return reportRepository.findByReportingManagerIdOrderByCreatedAtDesc(manager.getId()).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public FraudReportResponse updateStatus(Long id, UpdateFraudReportStatusRequest req) {
        FraudReport report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found: " + id));
        if (req.getStatus() == FraudReportStatus.OPEN) {
            throw new BadRequestException("Status must be RESOLVED or DISMISSED");
        }
        report.setStatus(req.getStatus());
        report.setAdminNotes(req.getAdminNotes());
        report.setResolvedAt(LocalDateTime.now());
        return toResponse(reportRepository.save(report));
    }

    public void delete(Long id) {
        if (!reportRepository.existsById(id)) {
            throw new ResourceNotFoundException("Report not found: " + id);
        }
        reportRepository.deleteById(id);
    }

    private FraudReportResponse toResponse(FraudReport r) {
        Restaurant rest = r.getRestaurant();
        return new FraudReportResponse(
                r.getId(),
                r.getReportedUser().getId(),
                r.getReportedUser().getUsername(),
                r.getReportingManager().getId(),
                r.getReportingManager().getUsername(),
                rest != null ? rest.getId() : null,
                rest != null ? rest.getName() : null,
                r.getRelatedOrderId(),
                r.getReason(),
                r.getAdminNotes(),
                r.getStatus(),
                r.getCreatedAt(),
                r.getResolvedAt()
        );
    }
}
