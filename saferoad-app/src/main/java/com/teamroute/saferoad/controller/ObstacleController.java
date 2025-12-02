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
public class ObstacleController {

    private final ObstacleService obstacleService;

    /**
     * 장애물 목록 페이지 (check-list)
     */
    @GetMapping("/check-list")
    public String checkList(Model model,
                            @RequestParam(value = "page", defaultValue = "0") int page) {

        // 페이징 및 정렬 정보 생성 (최신순)
        Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "id"));

        // Service 메서드가 (keyword, pageable)을 요구하므로,
        // 검색어가 없는 경우 null을 전달
        Page<ObstacleGroup> pagingResult = obstacleService.findAllObstacleGroups(null, pageable);

        model.addAttribute("obstacleGroups", pagingResult.getContent());
        model.addAttribute("pagingResult", pagingResult);

        return "check-list";
    }

    @GetMapping("/check-map")
    public String checkMap(Model model) {
        model.addAttribute("check-map", "check-map");
        return "check-map";
    }

    @GetMapping("/obstacle")
    public String obstacle(Model model) {
        model.addAttribute("obstacle", "obstacle");
        return "obstacle";
    }
}