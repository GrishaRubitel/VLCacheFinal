package ru.choomandco.VLCacheFinal.fileGarbage.kafkaConfig;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Класс необходим для перечисления существующих топиков. В нашем случае никаких топиков создано не будет. Мы уже всё создали на уровне конфигурации Docker-compose
 */
public class KafkaTopicConfig {

    public static final String DATA_TOPIC = "data-topic";

    @Bean
    public NewTopic downloaderService() {
        return TopicBuilder.name(DATA_TOPIC).build();
    }

}
