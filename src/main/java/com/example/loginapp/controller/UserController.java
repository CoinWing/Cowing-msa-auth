package com.example.loginapp.controller;

import com.example.loginapp.dto.RegisterUserDto;
import com.example.loginapp.entity.UserEntity;
import com.example.loginapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// import com.example.loginapp.dto.LoginResponseDto;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입
   @PostMapping("/signup")
public ResponseEntity<String> register(@RequestBody RegisterUserDto dto) {
    try {
        userService.registerUser(dto.email(), dto.passwd(), dto.nickname());
        return ResponseEntity.ok("회원가입 성공");
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body("회원가입 실패: " + e.getMessage());
    }
}

    // @PostMapping("/signin")
    // public ResponseEntity<?> signin(
    //         @RequestParam String email,
    //         @RequestParam String passwd) {

    //     return userService.loginAndReturnUser(email, passwd)
    //             .map(ResponseEntity::ok)
    //             .orElseGet(() -> ResponseEntity.status(401).body(null));
    // }

    // @PutMapping("/change/password")
    // public ResponseEntity<String> changePassword(
    //         @RequestParam String email,
    //         @RequestParam String currentPwd,
    //         @RequestParam String newPwd) {

    //     boolean result = userService.updatePassword(email, currentPwd, newPwd);

    //     if (result) {
    //         return ResponseEntity.ok("비밀번호 변경 성공");
    //     } else {
    //         return ResponseEntity.status(401).body("현재 비밀번호가 일치하지 않거나 사용자 없음");
    //     }
    // }
}