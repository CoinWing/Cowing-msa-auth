package cowing.auth.service;

import cowing.auth.dto.LoginReqDto;
import cowing.auth.dto.TokenDto;
import cowing.auth.entity.User;
import cowing.auth.exception.RefreshTokenInvalidException;
import cowing.auth.jwt.JwtRedis;
import cowing.auth.jwt.TokenProvider;
import cowing.auth.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.hibernate.service.spi.ServiceException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final TokenProvider tokenProvider;
    private final JwtRedis jwtRedis;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public TokenDto login(LoginReqDto loginReqDto) {
        try {
            UsernamePasswordAuthenticationToken authenticationToken = loginReqDto.toAuthentication();
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            String username = loginReqDto.username();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

            if (user.getDeletedAt() != null) {
                throw new DisabledException("탈퇴한 사용자입니다.");
            }
            TokenDto tokens = tokenProvider.generateTokenDto(authentication);
            ValueOperations<String, String> ops = redisTemplate.opsForValue();
            ops.set(loginReqDto.username(), tokens.refreshToken(), Duration.ofDays(2));
            return tokens;
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("유효하지 않은 아이디와 비밀번호입니다.", e);
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("로그인 중 알 수 없는 에러가 발생했습니다.", e);
        }
    }

    @Transactional
    public TokenDto refresh(HttpServletRequest request) throws RefreshTokenInvalidException {
        String refreshToken = request.getHeader("Authorization").substring(7);
        if (refreshToken.isEmpty()) {
            throw new RefreshTokenInvalidException("Refresh Token이 없습니다.");
        }
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Token이 유효하지 않습니다.");
        }
        String username = tokenProvider.getUserIdFromRefreshToken(refreshToken);
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));

        TokenDto tokens = tokenProvider.regenerateToken(user);
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set(user.getUsername(), tokens.refreshToken(), Duration.ofDays(2));
        return tokens;
    }
}