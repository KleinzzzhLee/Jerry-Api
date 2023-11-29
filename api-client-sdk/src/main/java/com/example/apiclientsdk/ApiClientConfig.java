package com.example.apiclientsdk;

import com.example.apiclientsdk.clients.ApiClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("api.client")
@Data
@ComponentScan
public class ApiClientConfig {
    private String appKey;
    private String appSecret;

    @Bean
    public ApiClient getApiClient() {
        return new ApiClient(appKey, appSecret);
    }
}
