package com.teamroute.saferoad.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MypageFormDTO {

    // 조회용 필드 (프로필 카드)
    private String userid;
    private String initial; // 이름 첫 글자 (아바타용)

    // 수정용 폼 필드 (계정 정보)
    private String name;

    // 비밀번호 변경 (비어있을 수 있음)
    private String password = "";
    private String passwordConfirm = "";

}