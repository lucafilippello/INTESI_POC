package com.intesi.userservice.service;

import com.intesi.userservice.dto.ActivityLogDto;
import com.intesi.userservice.mapper.ActivityLogMapper;
import com.intesi.userservice.model.ActivityType;
import com.intesi.userservice.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LogService {

    private final ActivityLogRepository activityLogRepository;
    private final ActivityLogMapper activityLogMapper;
    
    public List<ActivityLogDto> findAll() {
    	Sort sort = Sort.by(Sort.Direction.ASC, "activityDateTime");
        return activityLogRepository.findAll(sort).stream()
                .map(activityLogMapper::toDto)
                .collect(Collectors.toList());
    }
    
    public List<ActivityLogDto> findByLoggedUser(String loggedUser) {
        return activityLogRepository.findByActivityOwnerOrderByActivityDateTime(loggedUser).stream()
                .map(activityLogMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ActivityLogDto> findByActivityType(ActivityType type) {
        return activityLogRepository.findByActivityTypeOrderByActivityDateTime(type).stream()
                .map(activityLogMapper::toDto)
                .collect(Collectors.toList());
    }
}
