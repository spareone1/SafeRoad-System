package com.example.teamroute.domain.obstacle.controller;

import com.example.teamroute.domain.obstacle.entity.Obstacle;
import com.example.teamroute.domain.obstacle.repository.ObstacleRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/obstacles")
public class ObstacleController {

    private final ObstacleRepository obstacleRepository;

    public ObstacleController(ObstacleRepository obstacleRepository) {
        this.obstacleRepository = obstacleRepository;
    }

    // ✅ 전체 장애물 조회
    @GetMapping
    public List<Obstacle> getAllObstacles() {
        return obstacleRepository.findAll();
    }

    // ✅ 특정 ID로 장애물 조회
    @GetMapping("/{id}")
    public Obstacle getObstacleById(@PathVariable Long id) {
        return obstacleRepository.findById(id).orElse(null);
    }

    // ✅ 새 장애물 등록
    @PostMapping
    public Obstacle createObstacle(@RequestBody Obstacle obstacle) {
        return obstacleRepository.save(obstacle);
    }

    // ✅ 장애물 수정 (기본 update)
    @PutMapping("/{id}")
    public Obstacle updateObstacle(@PathVariable Long id, @RequestBody Obstacle updatedObstacle) {
        return obstacleRepository.findById(id)
                .map(obstacle -> {
                    obstacle.setObstacleName(updatedObstacle.getObstacleName());
                    obstacle.setLat(updatedObstacle.getLat());
                    obstacle.setLon(updatedObstacle.getLon());
                    obstacle.setImageloc(updatedObstacle.getImageloc());
                    obstacle.setUpdated_at(updatedObstacle.getUpdated_at());
                    return obstacleRepository.save(obstacle);
                })
                .orElseGet(() -> {
                    updatedObstacle.setId(id);
                    return obstacleRepository.save(updatedObstacle);
                });
    }

    // ✅ 장애물 삭제
    @DeleteMapping("/{id}")
    public void deleteObstacle(@PathVariable Long id) {
        obstacleRepository.deleteById(id);
    }
}
