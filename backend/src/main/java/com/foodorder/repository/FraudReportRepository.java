package com.foodorder.repository;

import com.foodorder.entity.FraudReport;
import com.foodorder.enums.FraudReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FraudReportRepository extends JpaRepository<FraudReport, Long> {
    List<FraudReport> findByReportingManagerIdOrderByCreatedAtDesc(Long managerId);
    List<FraudReport> findByStatusOrderByCreatedAtDesc(FraudReportStatus status);
    List<FraudReport> findAllByOrderByCreatedAtDesc();
}
