package com.movemate.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Exponer la carpeta externa 'uploads' para que se pueda acceder desde el navegador
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
