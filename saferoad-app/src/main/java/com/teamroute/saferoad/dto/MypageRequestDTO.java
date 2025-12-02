package com.teamroute.saferoad.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MypageRequestDTO {

    // <form>의 name 속성과 일치
    private String name;

    // 새 비밀번호 (비어있을 수 있음)
    private String password;
    private String passwordConfirm;

    private String email;
    private boolean emailpermission;

}