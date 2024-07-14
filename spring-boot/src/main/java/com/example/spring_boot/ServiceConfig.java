package com.example.spring_boot;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {
	@Bean
    public Service simulationService() {
        return new Service();
    }
}
