package com.app.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;


@Slf4j
@Configuration
public class AppConfig {

       public void init() {
        log.info("Application started successfully!");
        log.debug("Debug mode enabled for configuration");
    }
}