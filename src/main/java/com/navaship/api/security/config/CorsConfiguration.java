package com.navaship.api.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfiguration {
    @Value("${navaship.webapp.url}")
    private String webAppUrl;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Add the CORS configuration for the /api/v1/auth/refresh-token endpoint
                //                registry.addMapping("/api/v*/auth/refresh-token")
                //                        .allowedOrigins(webAppUrl)
                //                        .allowedMethods("POST")
                //                        .exposedHeaders("Set-Cookie")
                //                        .allowCredentials(true);

                registry.addMapping("/api/v*/**")
                        .allowedOrigins(webAppUrl)
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .exposedHeaders("Set-Cookie")
                        .allowCredentials(true);
            }
        };
    }
}
