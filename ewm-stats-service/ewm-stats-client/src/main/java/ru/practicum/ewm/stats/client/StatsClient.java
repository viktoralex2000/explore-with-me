package ru.practicum.ewm.stats.client;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;

import java.net.URI;
import java.util.Collections;
import java.util.List;

public class StatsClient {

    private final RestTemplate restTemplate;
    private final String serverUrl;

    public StatsClient(RestTemplate restTemplate, String serverUrl) {
        this.restTemplate = restTemplate;
        this.serverUrl = serverUrl;
    }

    public ResponseEntity<Void> hit(EndpointHitDto endpointHitDto) {
        URI uri = UriComponentsBuilder.fromHttpUrl(serverUrl)
                .path("/hit")
                .build()
                .toUri();

        return restTemplate.postForEntity(uri, endpointHitDto, Void.class);
    }

    public List<ViewStatsDto> getStats(String start,
                                       String end,
                                       @Nullable List<String> uris,
                                       @Nullable Boolean unique) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverUrl)
                .path("/stats")
                .queryParam("start", start)
                .queryParam("end", end);

        if (uris != null && !uris.isEmpty()) {
            builder.queryParam("uris", uris);
        }

        if (unique != null) {
            builder.queryParam("unique", unique);
        }

        URI uri = builder.build(true).toUri();

        ResponseEntity<List<ViewStatsDto>> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        return response.getBody() == null ? Collections.emptyList() : response.getBody();
    }
}