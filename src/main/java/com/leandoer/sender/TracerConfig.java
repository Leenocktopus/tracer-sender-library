package com.leandoer.sender;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

@Setter
@Getter
@Configuration
@ConfigurationProperties("tracer.config")
public class TracerConfig {
    private String application = null;
    private String host = "http://localhost:7111";
    private String path = "/traces";

    @Bean
    @ConditionalOnMissingBean(RestTemplate.class)
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(new ArrayList<>());
        return restTemplate;
    }


}
