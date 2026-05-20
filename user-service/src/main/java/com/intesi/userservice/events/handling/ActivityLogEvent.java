package com.intesi.userservice.events.handling;

import java.time.LocalDateTime;

import com.intesi.userservice.model.ActivityType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ActivityLogEvent {

    private String owner;
    private ActivityType type;
    private String message;
    private LocalDateTime activityDateTime;

}
