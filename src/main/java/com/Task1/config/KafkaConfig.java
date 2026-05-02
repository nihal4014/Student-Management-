package com.Task1.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Bean
    public NewTopic studentLoginTopic(@Value("${app.kafka.student-login-topic}") String topicName) {
        return new NewTopic(topicName, 1, (short) 1);
    }
}