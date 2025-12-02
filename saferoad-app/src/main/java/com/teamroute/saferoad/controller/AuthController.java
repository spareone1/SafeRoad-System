package com.teamroute.saferoad.controller;

import com.teamroute.saferoad.domain.User;
import com.teamroute.saferoad.dto.MypageFormDTO;
import com.teamroute.saferoad.dto.MyReportDTO;
import com.teamroute.saferoad.service.ObstacleService;
import com.teamroute.saferoad.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final ObstacleService obstacleService; // ObstacleService 주입

    // --- (페이지 이동 컨트롤러) ---

    @GetMapping("/login")
    public String loginPage(Model model) {
        return "login";
    }

    @GetMapping("/mypage")
    public String mypage(Model model, @AuthenticationPrincipal String userid) {

        if (userid == null) {
            return "redirect:/login";
        }

        try {
            User user = userService.findByUserid(userid)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자 정보를 찾을 수 없습니다."));

            MypageFormDTO userDto = new MypageFormDTO();
            userDto.setUserid(user.getUserid());
            userDto.setName(user.getName());

            if (user.getName() != null && !user.getName().isBlank()) {
                userDto.setInitial(user.getName().substring(0, 1).toUpperCase());
            } else {
                userDto.setInitial("?");
            }

            model.addAttribute("userDto", userDto);

            List<MyReportDTO> myReports = obstacleService.getMyRecentReports(userid);
            model.addAttribute("myReports", myReports);

            return "mypage";

        } catch (UsernameNotFoundException e) {
            return "redirect:/login";
        }
    }

    @GetMapping("/register")
    public String register(Model model) {
        return "register";
    }

    @GetMapping("/register-done")
    public String registerDone(Model model) {
        return "register-done";
    }

    @GetMapping("/withdraw")
    public String withdraw(Model model) {
        return "withdraw";
    }

    @GetMapping("/withdraw-success")
    public String withdrawSuccess() {
        return "withdraw-success";
    }

    // --- [마이페이지 기능 컨트롤러] ---

    @PostMapping("/mypage/update")
    public String updateMypage(
            @AuthenticationPrincipal String userid,
            @ModelAttribute MypageFormDTO dto,
            RedirectAttributes attrs) {

        if (userid == null) {
            return "redirect:/login";
        }

        try {
            userService.updateUser(userid, dto);
            attrs.addFlashAttribute("success", "회원 정보가 성공적으로 수정되었습니다.");
        } catch (IllegalArgumentException | UsernameNotFoundException e) {
            attrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/mypage";
    }

    @GetMapping("/withdraw/proceed")
    public String withdrawProcess(@AuthenticationPrincipal String userid, RedirectAttributes attrs) {
        if (userid == null) {
            return "redirect:/login";
        }

        try {
            userService.withdrawUser(userid);
            return "redirect:/withdraw-success";

        } catch (UsernameNotFoundException e) {
            attrs.addFlashAttribute("error", "사용자 처리 중 오류가 발생했습니다.");
            return "redirect:/mypage";
        }
    }

}