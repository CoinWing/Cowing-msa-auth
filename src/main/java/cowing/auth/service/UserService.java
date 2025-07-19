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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
            validateUser(email, username);
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

    private void validateUser(String email, String username) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
    }

    @Transactional
    public boolean updatePassword(String email, String currentPwd, String newPwd) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty())
            return false;

        User user = optionalUser.get();

        if (!passwordEncoder.matches(currentPwd, user.getPasswd())) {
            return false;
        }

        String hashedNewPwd = passwordEncoder.encode(newPwd);
        user.setPasswd(hashedNewPwd);
        userRepository.save(user);
        return true;
    }

    @Transactional
    public boolean updateNickname(String username, String nickname) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if(optionalUser.isEmpty())
            return false;

        User user = optionalUser.get();
        user.setNickname(nickname);
        return true;
    }

    @Transactional(readOnly = true)
    public List<PortfolioDto> getPortfolio(String username) {
        Optional<User> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isEmpty()) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }

        LocalDateTime bankruptAt = optionalUser.get().getBankruptAt();


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
    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(()-> new RuntimeException("사용자를 찾을 수 없습니다."));
        user.markAsDeleted();
    }

    @Transactional
    public void bankrupt(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(()-> new RuntimeException("사용자를 찾을 수 없습니다."));
        user.markAsBankrupt();
        user.setUHoldings(10000000L);
    }

}