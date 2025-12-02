package com.teamroute.saferoad.api;

import com.teamroute.saferoad.domain.ObstacleGroup;
import com.teamroute.saferoad.dto.MapMarkerDTO;
import com.teamroute.saferoad.dto.ObstacleGroupDetailDTO;
import com.teamroute.saferoad.dto.UpdateStateDTO;
import com.teamroute.saferoad.service.ObstacleService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/obstacle-group")
@RequiredArgsConstructor
public class ObstacleApiController {

    private final ObstacleService obstacleService;

    /**
     * 지도 마커 데이터 조회 API
     * 현재 보고 있는 지도 영역(Bounds) 내의 장애물만 조회
     *
     */
    @GetMapping("/markers")
    public ResponseEntity<List<MapMarkerDTO>> getMarkers(
            @RequestParam double minLat,
            @RequestParam double maxLat,
            @RequestParam double minLon,
            @RequestParam double maxLon
    ) {
        List<MapMarkerDTO> markers = obstacleService.findMarkersWithinBounds(minLat, maxLat, minLon, maxLon);
        return ResponseEntity.ok(markers);
    }

    /**
     * ObstacleGroup의 상세 정보 조회 (GET)
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getObstacleGroupDetails(@PathVariable Long id) {

        Optional<ObstacleGroup> groupOpt = obstacleService.findGroupDetailsById(id);

        if (groupOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ObstacleGroupDetailDTO dto = new ObstacleGroupDetailDTO(groupOpt.get());
        return ResponseEntity.ok(dto);
    }

    /**
     * ObstacleGroup의 처리 상태 업데이트 (PUT)
     */
    @PutMapping("/{id}/state")
    public ResponseEntity<?> updateObstacleGroupState(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStateDTO dto) {

        try {
            obstacleService.updateGroupState(id, dto);
            return ResponseEntity.ok(Map.of("ok", true, "message", "상태가 업데이트되었습니다."));

        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "message", e.getMessage()));
        }
    }

    /**
     * ObstacleGroup을 삭제 (DELETE)
     * 신고 반려 시 호출되며, DB 설정(ON DELETE CASCADE)에 따라 하위 Obstacle도 함께 삭제.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteObstacleGroup(@PathVariable Long id) {
        try {
            obstacleService.deleteGroup(id);
            return ResponseEntity.ok(Map.of("ok", true, "message", "삭제되었습니다."));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("ok", false, "message", "삭제 중 오류가 발생했습니다."));
        }
    }
}