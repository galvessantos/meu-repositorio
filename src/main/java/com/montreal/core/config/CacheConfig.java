package com.montreal.core.config;

import com.montreal.core.cache.GenericCache;
import com.montreal.core.cache.IGenericCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public <K, V> IGenericCache<K, V> getCache(@Value("${app.cache-timeout}") Long cacheTimeout) {
        return new GenericCache<>(cacheTimeout);
    }
}