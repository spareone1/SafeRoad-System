package com.example.teamroute.domain.obstacle.controller;

import com.example.teamroute.domain.obstacle.entity.ObstacleGroup;
import com.example.teamroute.domain.obstacle.repository.ObstacleGroupRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/obstaclegroups")
public class ObstacleGroupController {

    private final ObstacleGroupRepository obstacleGroupRepository;

    public ObstacleGroupController(ObstacleGroupRepository obstacleGroupRepository) {
        this.obstacleGroupRepository = obstacleGroupRepository;
    }

    // 전체 조회
    @GetMapping
    public List<ObstacleGroup> getAllGroups() {
        return obstacleGroupRepository.findAll();
    }

    // ID로 조회
    @GetMapping("/{id}")
    public ObstacleGroup getGroupById(@PathVariable Long id) {
        return obstacleGroupRepository.findById(id).orElse(null);
    }

    // 새 그룹 등록
    @PostMapping
    public ObstacleGroup createGroup(@RequestBody ObstacleGroup group) {
        return obstacleGroupRepository.save(group);
    }

    // 그룹 삭제
    @DeleteMapping("/{id}")
    public void deleteGroup(@PathVariable Long id) {
        obstacleGroupRepository.deleteById(id);
    }
}
