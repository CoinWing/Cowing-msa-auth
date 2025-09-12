package cowing.auth.service;

import cowing.auth.dto.PortfolioDto;
import cowing.auth.dto.UserInfoDto;
import cowing.auth.entity.Portfolio;
import cowing.auth.entity.User;
import cowing.auth.repository.PortfolioRepository;
import cowing.auth.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final PortfolioRepository portfolioRepository;

    public UserService(UserRepository userRepository, PortfolioRepository portfolioRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.portfolioRepository = portfolioRepository;
    }

    @Transactional
    public boolean registerUser(String email, String rawPassword, String nickname, String username) {
        try {
            validateUser(username);
            String hashedPassword = passwordEncoder.encode(rawPassword);
            User user = User.builder()
                    .email(email)
                    .passwd(hashedPassword)
                    .nickname(nickname)
                    .username(username)
                    .uHoldings(10000000L)
                    .build();
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            System.out.println("회원가입 실패: " + e.getMessage());
            return false;
        }
    }

    private void validateUser(String username) {
        if (userRepository.existsUserByUsername(username)) {
            throw new IllegalArgumentException("이미 존재하는 사용자입니다.");
        }
    }

    @Transactional
    public void updatePassword(String username, String currentPwd, String newPwd) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(currentPwd, user.getPasswd())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        String hashedNewPwd = passwordEncoder.encode(newPwd);
        user.setPasswd(hashedNewPwd);
        userRepository.save(user);
    }

    @Transactional
    public void updateNickname(String username, String nickname) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        user.setNickname(nickname);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<PortfolioDto> getPortfolio(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        LocalDateTime bankruptAt = user.getBankruptAt();


        List<Portfolio> portfolios;
        if (bankruptAt == null) {
            portfolios = portfolioRepository.findByUsername(username);
        } else {
            portfolios = portfolioRepository.findByUsernameAndCreatedAtAfter(username, bankruptAt);
        }

        return portfolios.stream()
                .map(p -> new PortfolioDto(
                        p.getMarketCode(),
                        p.getQuantity(),
                        p.getAverageCost(),
                        p.getTotalCost()
                ))
                .toList();

    }

    @Transactional(readOnly = true)
    public Long getUserAsset(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return user.getUHoldings();
    }

    @Transactional(readOnly = true)
    public UserInfoDto getUserInfo(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return new UserInfoDto(
                user.getNickname(),
                user.getEmail(),
                user.getUsername()
        );
    }

    @Transactional
    public void markAsDeletedUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(()-> new RuntimeException("사용자를 찾을 수 없습니다."));
        user.markAsDeleted();
        userRepository.save(user);
    }

    @Transactional
    public void bankrupt(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(()-> new RuntimeException("사용자를 찾을 수 없습니다."));
        user.markAsBankrupt();
        user.setUHoldings(10000000L);
        userRepository.save(user);
    }

}