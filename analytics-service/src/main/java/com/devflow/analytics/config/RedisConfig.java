package com.devflow.analytics.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    // RedisTemplate = Spring's main class for Redis operations
    // Like JdbcTemplate for databases, KafkaTemplate for Kafka
    // RedisTemplate<String, Object>:
    //   String = key type (all our keys are strings)
    //   Object = value type (could be String, Map, Long etc.)
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, Object> template =
                new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // StringRedisSerializer = keys stored as plain strings
        // "analytics:total-orders" not as binary/bytes
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Jackson serializer = values stored as JSON
        // Java Long 142 → stored as "142" in Redis
        // Java Map → stored as "{...}" JSON in Redis
        ObjectMapper objectMapper = new ObjectMapper();
        // JavaTimeModule = handles LocalDateTime serialization
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}