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

    /**
     * /api/admin/metrics 전용 클라이언트.
     * 최대 200명 순차 추천 연산으로 응답이 30~60초 걸릴 수 있어 readTimeout을 길게 둔다.
     * 다른 Flask 호출(flaskRestClient)에는 영향을 주지 않는다.
     */
    @Bean
    public RestClient flaskAdminMetricsRestClient(){
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5)) //연결 자체는 빠르므로 5초 유지
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(60)); //응답 대기 시간 60초

        return RestClient.builder()
                .baseUrl(flaskBaseUrl)
                .requestFactory(factory)
                .build();
    }
}