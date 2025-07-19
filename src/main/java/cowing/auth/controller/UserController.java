package cowing.auth.controller;

import cowing.auth.dto.*;
import cowing.auth.entity.PrincipalDetails;
import cowing.auth.entity.User;
import cowing.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입
    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임, 유저명을 입력하여 회원가입")
    @ApiResponse(responseCode = "201", description = "회원가입 성공")
    @ApiResponse(responseCode = "400", description = "회원가입 실패")
    @PostMapping("/signup")
    public ResponseEntity<?> register(@RequestBody RegisterUserDto dto) {
        boolean result = userService.registerUser(dto.email(), dto.passwd(), dto.nickname(), dto.username());
        if (result) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 201);
            response.put("message", "회원가입에 성공하였습니다. 축하드립니다!");
            return ResponseEntity.status(201).body(response);
        } else {
            return ResponseEntity.badRequest().body("회원가입 실패");
        }
    }

    // 비밀번호 변경
    @Operation(summary = "비밀번호 변경", description = "이메일과 현재 비밀번호를 확인하여 비밀번호 변경")
    @ApiResponse(responseCode = "201", description = "비밀번호 변경 성공")
    @ApiResponse(responseCode = "400", description = "현재 비밀번호 혹은 이메일 오입력으로 실패")
    @PostMapping("/change/passwd")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeDto dto) {
        boolean result = userService.updatePassword(dto.email(), dto.currentPwd(), dto.newPwd());
        if (result) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 201);
            response.put("message", "비밀번호가 변경되었습니다!");
            return ResponseEntity.status(201).body(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", "현재 비밀번호 혹은 이메일이 잘못되었습니다.");
            return ResponseEntity.status(400).body(response);
        }
    }

    @Operation(summary = "비밀번호 변경", description = "이메일로 유저 조회해서 닉네임 변경")
    @ApiResponse(responseCode = "201", description = "닉네임 변경 성공")
    @ApiResponse(responseCode = "400", description = "닉네임 변경 실패")
    @PostMapping("/change/nickname")
    public ResponseEntity<?> changeNickname(@AuthenticationPrincipal PrincipalDetails principal, @RequestBody NicknameChangeDto dto) {
        boolean result = userService.updateNickname(principal.getUsername(), dto.nickname());
        if (result) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 201);
            response.put("message", "닉네임이 변경되었습니다!");
            return ResponseEntity.status(201).body(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", "닉네임 변경에 실패했습니다");
            return ResponseEntity.status(400).body(response);
        }
    }

    // 유저 포트폴리오 조회
    @Operation(summary = "포트폴리오 조회", description = "로그인된 유저의 포트폴리오 조회")
    @ApiResponse(responseCode = "200", description = "포트폴리오 조회 성공, 아직 거래내역 없을 시 존재하지 않는다는 메세지와 빈 배열 전달")
    @GetMapping("/portfolio")
    public ResponseEntity<?> getPortfolio(@AuthenticationPrincipal PrincipalDetails principal) {
        String username = principal.getUsername();
        List<PortfolioDto> portfolios = userService.getPortfolio(username);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        if (!portfolios.isEmpty()) {
            response.put("message", "포트폴리오 조회 성공");
        } else {
            response.put("message", "포트폴리오가 존재하지 않습니다.");
        }
        response.put("data", portfolios);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "원화 자산 조회", description = "로그인된 유저의 원화(KRW) 자산 조회")
    @ApiResponse(responseCode = "200", description = "원화 자산 조회 성공")
    @GetMapping("/asset")
    public ResponseEntity<?> getKRWHoldings(@AuthenticationPrincipal PrincipalDetails principal) {
        String username = principal.getUsername();

        Long asset = userService.getUserAsset(username);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "원화 자산 조회 성공");
        response.put("asset", asset);

        return ResponseEntity.ok(response);
    }
    @Operation(summary = "유저 정보 조회", description = "로그인된 유저의 기본 정보 조회")
    @ApiResponse(responseCode = "200", description = "유저 정보 조회 성공")
    @GetMapping("/infos")
    public ResponseEntity<?> getUserInfo(@AuthenticationPrincipal PrincipalDetails principal) {
        String username = principal.getUsername();
        UserInfoDto userInfo = userService.getUserInfo(username);

        return ResponseEntity.ok(userInfo);
    }

    @Operation(summary = "계정 탈퇴", description = "로그인된 유저의 계정 탈퇴")
    @ApiResponse(responseCode = "200", description = "탈퇴 성공")
    @PostMapping("/deletion")
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal PrincipalDetails principal) {
        String username = principal.getUsername();
        userService.deleteUser(username);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "계정이 성공적으로 삭제되었습니다.");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "파산 신청", description = "로그인된 유저의 계정 탈퇴")
    @ApiResponse(responseCode = "200", description = "파산 신청 성공")
    @PostMapping("/bankrupt")
    public ResponseEntity<?> bankrupt(@AuthenticationPrincipal PrincipalDetails principal) {
        String username = principal.getUsername();
        userService.bankrupt(username);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "파산 신청이 완료되었습니다.");

        return ResponseEntity.ok(response);
    }

}