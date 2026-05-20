package com.intesi.userservice.repository;

import com.intesi.userservice.model.ActivityLog;
import com.intesi.userservice.model.ActivityType;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    
    List<ActivityLog> findByActivityOwnerOrderByActivityDateTime(String loggedUser);
    
    List<ActivityLog> findByActivityTypeOrderByActivityDateTime(ActivityType activityType);
}
