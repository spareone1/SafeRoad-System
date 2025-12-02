package com.teamroute.saferoad.service;

import com.teamroute.saferoad.dto.NaverSearchDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class NaverSearchService {

    @Value("${naver.search.client-id}")
    private String clientId;

    @Value("${naver.search.client-secret}")
    private String clientSecret;

    @Value("${naver.search.url}")
    private String apiUrl;

    public NaverSearchDTO searchPlace(String query) {
        // RestTemplate 생성
        RestTemplate restTemplate = new RestTemplate();

        // 헤더 설정 (Client ID, Secret)
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);

        // URL 생성 (쿼리 파라미터 인코딩)
        URI uri = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .queryParam("query", query)
                .queryParam("display", 5) // 검색 결과 개수 (5개)
                .queryParam("start", 1)
                .queryParam("sort", "random") // 정확도순
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();

        // 요청 생성
        RequestEntity<Void> req = RequestEntity
                .get(uri)
                .headers(headers)
                .build();

        // API 호출 및 응답 파싱
        ResponseEntity<NaverSearchDTO> response = restTemplate.exchange(req, NaverSearchDTO.class);

        return response.getBody();
    }
}