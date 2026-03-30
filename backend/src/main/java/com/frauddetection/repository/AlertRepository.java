package com.frauddetection.repository;

import com.frauddetection.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, String> {

    List<Alert> findTop20ByOrderByCreatedAtDesc();

    List<Alert> findByStatusOrderByCreatedAtDesc(String status);

    long countByStatus(String status);

    @Query("SELECT a.severity, COUNT(a) FROM Alert a GROUP BY a.severity")
    List<Object[]> countBySeverity();

    @Query("SELECT a.alertType, COUNT(a) FROM Alert a GROUP BY a.alertType ORDER BY COUNT(a) DESC")
    List<Object[]> countByAlertType();
}
