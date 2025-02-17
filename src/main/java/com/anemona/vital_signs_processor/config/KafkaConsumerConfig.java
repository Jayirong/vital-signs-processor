package com.anemona.vital_signs_processor.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import com.anemona.vital_signs_processor.dto.EstadoVitalDTO;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;

// config de kafka y otros beans
@EnableKafka
@Configuration
public class KafkaConsumerConfig {
    
    @Bean
    public ConsumerFactory<String, EstadoVitalDTO> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka-1:9092,kafka-2:9093,kafka-3:9094");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "vital-signs-group");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, org.springframework.kafka.support.serializer.JsonDeserializer.class);
        config.put("spring.json.trusted.packages", "*");

        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EstadoVitalDTO> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, EstadoVitalDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

}
