package com.intesi.userservice.events.handling;

import java.time.LocalDateTime;


import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.intesi.userservice.config.RabbitMQConfig;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.intesi.userservice.model.ActivityType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ActivityLogEventProducer {

    private final RabbitTemplate rabbitTemplate;
    
    public void sendUserCreatedEvent(String activityMsg) {
		sendActivityEvent(ActivityType.USER_CREATION, activityMsg);
	}
    
    public void sendUserDeletedEvent(String activityMsg) {
		sendActivityEvent(ActivityType.USER_DELETION, activityMsg);
	}
    
    public void sendUserUpdatedEvent(String activityMsg) {
    	sendActivityEvent(ActivityType.USER_UPDATE, activityMsg);
    }
    
    public void sendRoleAddedEvent(String activityMsg) {
		sendActivityEvent(ActivityType.ROLE_ADD, activityMsg);
	}
    
    public void sendRoleRemovedEvent(String activityMsg) {
    	sendActivityEvent(ActivityType.ROLE_REMOVAL, activityMsg);
    }
    
    private void sendActivityEvent(ActivityType type, String message) {
        ActivityLogEvent event = new ActivityLogEvent();
        event.setOwner(getLoggedUser());
        event.setActivityDateTime(LocalDateTime.now());
        event.setType(type);
        event.setMessage(message);

        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, event);
    }

    private String getLoggedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "anonymous";
        }
        return auth.getName();
    }
}
