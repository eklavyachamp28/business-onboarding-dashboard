package com.aayusheklavya.onboarding.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface BusinessApplicationRepository extends JpaRepository<BusinessApplication, UUID> {

    List<BusinessApplication> findByStatusOrderByUpdatedAtDesc(ApplicationStatus status);

    List<BusinessApplication> findAllByOrderByUpdatedAtDesc();

    @Query("select a.status as status, count(a) as total from BusinessApplication a group by a.status")
    List<StatusCount> countByStatus();

    interface StatusCount {
        ApplicationStatus getStatus();
        long getTotal();
    }
}
