package com.recipe.infra.recommended;

import com.recipe.domain.dto.FlaskRecommendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class FlaskRecommenderClient {

    private final RestClient flaskRestClient; // Config에서 만든 빈 주입

    public FlaskRecommendResponse recommend(Long userId, int k, double lambda) {
        return flaskRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/recommend")
                        .queryParam("userId", userId)
                        .queryParam("k", k)
                        .queryParam("lambda", lambda)
                        .build())
                .retrieve()
                .body(FlaskRecommendResponse.class);
    }
}