package com.catolicasc.agrbackend.config.events;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncSpringEventConfig {

    public static final int CORE_POOL_SIZE = 4;
    public static final int MAX_POOL_SIZE = 8;

    @Bean(name = "applicationEventMulticaster")
    public ApplicationEventMulticaster threadPoolEventMulticaster() {

        var eventMulticaster = new SimpleApplicationEventMulticaster();

        // TODO: Problema potencial, necessário rever parametrização
        var threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(CORE_POOL_SIZE);
        threadPoolTaskExecutor.setMaxPoolSize(MAX_POOL_SIZE);
        threadPoolTaskExecutor.initialize();

        eventMulticaster.setTaskExecutor(threadPoolTaskExecutor);

        return eventMulticaster;
    }

}
