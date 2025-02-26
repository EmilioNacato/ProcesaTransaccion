package com.banquito.paymentprocessor.procesatransaccion.banquito.config;

import com.banquito.paymentprocessor.procesatransaccion.banquito.model.Transaccion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Transaccion> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Transaccion> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Transaccion.class));
        return template;
    }
} 