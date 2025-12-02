package com.example.teamroute.domain.obstacle.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GeocodingService {

    private final String googleApiKey;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    // 구글 API 주소
    private static final String GOOGLE_API_URL = "https://maps.googleapis.com/maps/api/geocode/json";

    public GeocodingService(
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper,
            @Value("${google.api-key}") String googleApiKey // yml에서 키 가져옴
    ) {
        this.googleApiKey = googleApiKey;
        this.objectMapper = objectMapper;
        this.webClient = webClientBuilder.baseUrl(GOOGLE_API_URL).build();
    }

    public String getAddressFromCoords(double lat, double lon) {
        // ★ 구글은 (위도,경도) 순서 (띄어쓰기 없이 콤마로 연결)
        String latlng = lat + "," + lon;

        log.info("구글 지오코딩 요청: latlng={}, KeyCheck={}", latlng, googleApiKey.substring(0, 5) + "...");

        try {
            String jsonResponse = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("latlng", latlng)
                            .queryParam("language", "ko") // 한국어 응답
                            .queryParam("key", googleApiKey) // 인증 키
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseGoogleAddress(jsonResponse);

        } catch (Exception e) {
            log.error("구글 API 호출 실패: {}", e.getMessage());
            return "주소 변환 실패";
        }
    }

    private String parseGoogleAddress(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            String status = root.path("status").asText();

            if (!"OK".equals(status)) {
                log.error("구글 API 에러 응답: {}", status);
                return "주소 정보 없음 (" + status + ")";
            }

            // results 배열의 첫 번째 결과의 formatted_address 가져오기
            // 구글은 친절하게 전체 주소를 한 줄로 만들어줍니다.
            JsonNode results = root.path("results");
            if (results.isArray() && results.size() > 0) {
                return results.get(0).path("formatted_address").asText();
            }

        } catch (Exception e) {
            log.error("JSON 파싱 오류: {}", e.getMessage());
        }
        return "주소 정보 부족";
    }
}