package com.example.loginapp.service;

import com.example.loginapp.entity.UserEntity;
import com.example.loginapp.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.loginapp.dto.LoginResponseDto;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // 회원가입
    public boolean registerUser(String email, String rawPassword, String nickname) {
        if (userRepository.existsByEmail(email)) {
            return false; // 이미 존재하는 이메일이면 실패
        }
        String hashedPassword = passwordEncoder.encode(rawPassword);
        UserEntity user = UserEntity.builder()
                .email(email)
                .passwd(hashedPassword)
                .nickname(nickname)
                .build();
        userRepository.save(user);
        return true;
    }

    // // 로그인
    // public boolean login(String email, String rawPassword) {
    //     return userRepository.findByEmail(email)
    //             .map(user -> passwordEncoder.matches(rawPassword, user.getPasswd()))
    //             .orElse(false);
    // }

    // // 로그인 user_id / nickname 리턴
    // public Optional<LoginResponseDto> loginAndReturnUser(String email, String rawPassword) {
    //     return userRepository.findByEmail(email)
    //             .filter(user -> passwordEncoder.matches(rawPassword, user.getPasswd()))
    //             .map(user -> new LoginResponseDto(user.getId(), user.getNickname()));
    // }

    // // 비밀번호 변경
    // public boolean updatePassword(String email, String currentPwd, String newPwd) {
    //     return userRepository.findByEmail(email)
    //             .filter(user -> passwordEncoder.matches(currentPwd, user.getPasswd()))
    //             .map(user -> {
    //                 String hashedNewPwd = passwordEncoder.encode(newPwd);
    //                 user.updatePassword(hashedNewPwd);
    //                 userRepository.save(user);
    //                 return true;
    //             })
    //             .orElse(false);
    // }
}