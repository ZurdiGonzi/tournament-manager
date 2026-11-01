package com.tournament.app.infrastructure.adapter.scryfall;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class ScryfallConfig {

    @Bean
    public RestClient scryfallRestClient(
        RestClient.Builder builder,
        @Value("${scryfall.base-url:https://api.scryfall.com}") String baseUrl,
        @Value("${scryfall.user-agent:TournamentManagerBackend/1.0}") String userAgent
    ) {
        return builder
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.USER_AGENT, userAgent)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
}
