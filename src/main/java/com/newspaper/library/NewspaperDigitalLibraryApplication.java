package com.newspaper.library;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Slf4j
@SpringBootApplication
public class NewspaperDigitalLibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewspaperDigitalLibraryApplication.class, args);
    }
}

@Component
@Slf4j
class StartupLogger {
    
    private final Environment env;
    
    public StartupLogger(Environment env) {
        this.env = env;
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String port = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("spring.mvc.servlet.path", "");
        
        log.info("Application started");
        log.info("Swagger UI: http://localhost:{}{}/swagger-ui.html", port, contextPath);
    }
}
