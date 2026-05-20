package com.intesi.userservice.events.handling;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import com.intesi.userservice.config.RabbitMQConfig;
// ===== fine RABBITMQ =====

import org.springframework.stereotype.Component;

import com.intesi.userservice.model.ActivityLog;
import com.intesi.userservice.repository.ActivityLogRepository;

@Component
public class ActivityLogEventConsumer {

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogEventConsumer(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void consume(ActivityLogEvent event) {
        ActivityLog dbLog = new ActivityLog();
        dbLog.setActivityOwner(event.getOwner());
        dbLog.setActivityType(event.getType());
        dbLog.setMessage(event.getMessage());
        dbLog.setActivityDateTime(event.getActivityDateTime());
        activityLogRepository.save(dbLog);
    }
}
