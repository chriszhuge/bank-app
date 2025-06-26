package org.bank.common.config;

import io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigCustomizer;
import org.bank.common.exception.TransactionException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class Resilience4jConfig {

    @Bean
    public CircuitBreakerConfigCustomizer transactionServiceCustomizer() {
        return CircuitBreakerConfigCustomizer
                .of("transactionService", builder -> builder
                        .ignoreExceptions(RuntimeException.class) // 忽略业务异常
                        .automaticTransitionFromOpenToHalfOpenEnabled(true)
                );
    }
}