package com.teamroute.saferoad.controller;

import com.teamroute.saferoad.domain.ObstacleGroup;
import com.teamroute.saferoad.service.ObstacleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final ObstacleService obstacleService;

    /**
     * 관리자 장애물 처리 페이지 (검색, 페이징, 정렬)
     * @param page 페이지 번호
     * @param keyword 검색어 (선택 사항)
     */
    @GetMapping("/admin")
    public String admin(Model model,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "keyword", required = false) String keyword) {

        // 페이징 정보 생성 (최신순)
        Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "id"));

        // 서비스 호출 시 keyword도 함께 전달
        Page<ObstacleGroup> pagingResult = obstacleService.findAllObstacleGroups(keyword, pageable);

        model.addAttribute("obstacleGroups", pagingResult.getContent());
        model.addAttribute("pagingResult", pagingResult);

        // 검색어를 템플릿에 다시 전달 (검색창에 유지하기 위함)
        model.addAttribute("keyword", keyword);

        return "admin";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("dashboard", "dashboard");
        return "dashboard";
    }
}