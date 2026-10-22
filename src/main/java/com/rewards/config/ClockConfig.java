package com.rewards.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Provides the application clock for date-dependent behavior. */
@Configuration
public class ClockConfig {
    /**
     * Creates a clock using the system's default time zone.
     *
     * @return the system clock for the default time zone
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}