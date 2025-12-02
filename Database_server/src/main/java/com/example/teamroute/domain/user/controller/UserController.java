package com.example.teamroute.domain.user.controller;

import com.example.teamroute.domain.user.dto.UserCreateRequestDto;
import com.example.teamroute.domain.user.dto.UserResponseDto;
import com.example.teamroute.domain.user.entity.User;
import com.example.teamroute.domain.user.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 1. 전체 조회 (List<Entity> -> List<DTO>)
    @GetMapping
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponseDto::new) // 하나씩 DTO로 포장
                .collect(Collectors.toList());
    }

    // 2. 회원 가입 (RequestDTO 받아서 -> ResponseDTO 반환)
    @PostMapping
    public UserResponseDto createUser(@RequestBody UserCreateRequestDto requestDto) {
        // 1) DTO를 진짜 User(Entity)로 변환
        User user = requestDto.toEntity();

        // 2) DB에 저장
        User savedUser = userRepository.save(user);

        // 3) 저장된 정보를 다시 DTO로 변환해서 반환 (비번 제외된 정보)
        return new UserResponseDto(savedUser);
    }

    // 3. 단건 조회 (Entity -> DTO)
    @GetMapping("/{id}")
    public UserResponseDto getUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(UserResponseDto::new) // 찾았으면 DTO로 변환
                .orElse(null); // 없으면 null (나중엔 에러 처리가 필요해)
    }

    // 4. 삭제 (반환값 없음, 그대로 유지)
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
    }
}