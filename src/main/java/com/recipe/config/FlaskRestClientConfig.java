package com.recipe.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class FlaskRestClientConfig {

    @Value("${FLASK_BASE_URL:http://127.0.0.1:5000}")
    private String flaskBaseUrl;

    @Bean
    public RestClient flaskRestClient(){
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2)) //서버에 연결이 맺어질 때까지 기다리는 시간
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(3)); //응답이 올 때까지 기다리는 시간

        return RestClient.builder()
                .baseUrl(flaskBaseUrl)
                .requestFactory(factory)
                .build();
    }
}