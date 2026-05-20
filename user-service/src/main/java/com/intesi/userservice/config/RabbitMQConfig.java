package com.intesi.userservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configurazione RabbitMQ.
 *
 * Topologia:
 *   Exchange  : activity-exchange  (TopicExchange, durevole)
 *   Queue     : activity-log-queue (durevole)
 *   Routing key: activity.log
 *
 * Il producer invia su (exchange + routing key),
 * il consumer legge dalla queue tramite il binding.
 */
@Configuration
@Profile("!test")
public class RabbitMQConfig {

    public static final String EXCHANGE    = "activity-exchange";
    public static final String QUEUE       = "activity-log-queue";
    public static final String ROUTING_KEY = "activity.log";

    // --- Topologia ---

    @Bean
    public TopicExchange activityExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue activityLogQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    public Binding activityLogBinding(Queue activityLogQueue, TopicExchange activityExchange) {
        return BindingBuilder
                .bind(activityLogQueue)
                .to(activityExchange)
                .with(ROUTING_KEY);
    }

    // --- Serializzazione JSON (con supporto per LocalDateTime) ---

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // necessario per serializzare LocalDateTime
        return new Jackson2JsonMessageConverter(mapper);
    }

    // --- Producer: RabbitTemplate con converter JSON ---

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    // --- Consumer: listener container factory con converter JSON ---

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }
}
