package com.teamroute.saferoad.service;

import com.teamroute.saferoad.domain.User;
import com.teamroute.saferoad.domain.User.UserStatus;
import com.teamroute.saferoad.dto.LoginRequestDTO;
import com.teamroute.saferoad.dto.MypageFormDTO;
import com.teamroute.saferoad.dto.UserRequestDTO;
import com.teamroute.saferoad.repository.UserRepository;
import com.teamroute.saferoad.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userid) throws UsernameNotFoundException {
        User user = userRepository.findByUserid(userid)
                .orElseThrow(() -> new UsernameNotFoundException("아이디를 찾을 수 없습니다: " + userid));

        return new CustomUserDetails(user);
    }

    @Transactional(readOnly = true)
    public User login(LoginRequestDTO dto) {
        User user = userRepository.findByUserid(dto.getUserid())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다."));

        if (!encoder.matches(dto.getPassword(), user.getPw())) {
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        return user;
    }

    @Transactional(readOnly = true)
    public boolean checkUseridDuplicate(String userid) {
        return userRepository.existsByUserid(userid);
    }

    @Transactional
    public User register(UserRequestDTO dto) {
        if (checkUseridDuplicate(dto.getUserid())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (!dto.getPassword().equals(dto.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        User newUser = User.builder()
                .userid(dto.getUserid())
                .pw(encoder.encode(dto.getPassword()))
                .name(dto.getName())
                .userstatus(UserStatus.normal_user)
                .build();
        return userRepository.save(newUser);
    }

    // --- [Mypage 기능] ---

    /**
     * 회원 정보 수정 (Mypage)
     * @param dto 폼에서 받은 수정 정보 (MypageFormDTO)
     */
    @Transactional
    public User updateUser(String userid, MypageFormDTO dto) {
        User user = userRepository.findByUserid(userid)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        user.setName(dto.getName());

        // 비밀번호는 입력된 경우에만 업데이트
        String newPassword = dto.getPassword();
        if (StringUtils.hasText(newPassword)) {
            if (!newPassword.equals(dto.getPasswordConfirm())) {
                throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
            }
            user.setPw(encoder.encode(newPassword));
        }

        return user;
    }

    /**
     * 회원 탈퇴 (Soft Delete)
     */
    @Transactional
    public void withdrawUser(String userid) {
        User user = userRepository.findByUserid(userid)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        user.setUserstatus(UserStatus.deleted);
        user.setUserid("deleted_" + user.getId() + "_" + user.getUserid());
        user.setPw(encoder.encode("DELETED_USER_PASSWORD"));

        userRepository.save(user);
    }

    public Optional<User> findByUserid(String userid) {
        return userRepository.findByUserid(userid);
    }

    public User save(User u) { return userRepository.save(u); }

    @Transactional
    public boolean changePassword(String userid, String currentRaw, String nextRaw) {
        var opt = userRepository.findByUserid(userid);
        if (opt.isEmpty()) return false;
        var u = opt.get();
        if (!encoder.matches(currentRaw, u.getPw())) {
            return false;
        }
        u.setPw(encoder.encode(nextRaw));
        userRepository.save(u);
        return true;
    }

    public String encode(String raw) { return encoder.encode(raw); }
}