package com.teamroute.saferoad.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequestDTO {

    // register.html의 <form> 내부 name 속성과 일치
    private String name;
    private String userid;
    private String password;
    private String passwordConfirm; // 비밀번호 확인용

    // required 체크박스
    private boolean terms;
    private boolean privacy;

}