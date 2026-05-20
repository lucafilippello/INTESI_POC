package com.intesi.userservice.dto;

import java.time.LocalDateTime;

import com.intesi.userservice.model.ActivityType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ActivityLogDto {
	
    private Long id;

    private String activityOwner;
    
    private ActivityType activityType;

    private String message;
    
    private LocalDateTime activityDateTime;

}
