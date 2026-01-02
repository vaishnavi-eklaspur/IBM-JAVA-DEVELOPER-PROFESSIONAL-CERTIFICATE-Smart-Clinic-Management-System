package com.ibm.smartclinic.backend.config;

import com.ibm.smartclinic.backend.logging.RequestLoggingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final RequestLoggingInterceptor requestLoggingInterceptor;

    public WebConfig(@NonNull RequestLoggingInterceptor requestLoggingInterceptor) {
        this.requestLoggingInterceptor = requestLoggingInterceptor;
    }

    /**
     * Spring Framework 6 guarantees non-null at runtime; warning is a known static-analysis limitation.
     */
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor((HandlerInterceptor) requestLoggingInterceptor) // documented cast for null-safety contract
                .addPathPatterns("/**");
    }
}
