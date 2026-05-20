package com.intesi.userservice.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "activity_logs")
@Getter
@Setter
@NoArgsConstructor
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="owner", nullable = false)
    private String activityOwner;
    
    @Enumerated(EnumType.STRING)
    @Column(name="type", nullable = false)
    private ActivityType activityType;

    @Column(nullable = false)
    private String message;
    
    @Column(nullable = false)
    private LocalDateTime activityDateTime;
}
