package com.evently.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter(@Value("${app.cors.origins:*}") String origins) {
        CorsConfiguration cfg = new CorsConfiguration();
        if ("*".equals(origins)) {
            cfg.addAllowedOriginPattern("*");
        } else {
            Arrays.stream(origins.split(",")).map(String::trim).forEach(cfg::addAllowedOrigin);
        }
        cfg.addAllowedHeader("*");
        cfg.addAllowedMethod("*");
        cfg.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return new CorsFilter(source);
    }
}
