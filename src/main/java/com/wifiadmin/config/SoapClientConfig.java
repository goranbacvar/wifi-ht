package com.wifiadmin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.client.core.WebServiceTemplate;

@Configuration
public class SoapClientConfig {

    @Bean
    public WebServiceTemplate webServiceTemplate() {
        WebServiceTemplate template = new WebServiceTemplate();
        template.setDefaultUri("http://localhost:8080/platform");
        return template;
    }
}
