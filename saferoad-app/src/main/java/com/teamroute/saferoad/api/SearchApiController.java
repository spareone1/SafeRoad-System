package com.teamroute.saferoad.api;

import com.teamroute.saferoad.dto.NaverSearchDTO;
import com.teamroute.saferoad.service.NaverSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchApiController {

    private final NaverSearchService naverSearchService;

    /**
     * 장소 검색 API (네이버 검색 API 프록시)
     */
    @GetMapping("/place")
    public ResponseEntity<?> searchPlace(@RequestParam String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "검색어를 입력해주세요."));
        }

        try {
            NaverSearchDTO result = naverSearchService.searchPlace(query);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "검색 중 오류가 발생했습니다."));
        }
    }
}