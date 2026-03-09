package com.example.ecommerce.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisCacheConfig {

        private static final Duration DEFAULT_TTL = Duration.ofMinutes(10);

        @Bean
        public RedisCacheConfiguration redisCacheConfiguration() {
                return RedisCacheConfiguration.defaultCacheConfig()
                                .prefixCacheNameWith("v1::")
                                .entryTtl(DEFAULT_TTL)
                                .serializeValuesWith(SerializationPair.fromSerializer(redisSerializer()));
        }

        private GenericJackson2JsonRedisSerializer redisSerializer() {
                return new GenericJackson2JsonRedisSerializer(objectMapperForRedis());
        }

        private ObjectMapper objectMapperForRedis() {
                var ptv = BasicPolymorphicTypeValidator.builder()
                                .allowIfSubType("java.util.")
                                .allowIfSubType("java.lang.")
                                .allowIfSubType("java.math.")
                                .allowIfSubType("com.example.ecommerce.backend.")
                                .allowIfSubType("java.time.")
                                .build();

                return new ObjectMapper()
                                .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL,
                                                JsonTypeInfo.As.PROPERTY)
                                .registerModule(new JavaTimeModule())
                                .registerModule(new com.fasterxml.jackson.module.paramnames.ParameterNamesModule())
                                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        }
}

