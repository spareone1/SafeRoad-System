package com.teamroute.saferoad.dto;

import lombok.Data;
import java.util.List;

@Data
public class NaverSearchDTO {
    // 네이버 API 응답 중 필요한 'items' 리스트만 매핑
    private List<Item> items;

    @Data
    public static class Item {
        private String title;       // 장소명 (HTML 태그 포함)
        private String roadAddress; // 도로명 주소
        private String address;     // 지번 주소
        private int mapx;           // KATECH 좌표 X
        private int mapy;           // KATECH 좌표 Y
    }
}