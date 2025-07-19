package cowing.auth.repository;

import cowing.auth.dto.PortfolioDto;
import cowing.auth.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    List<Portfolio> findByUsername(String username);
    List<Portfolio> findByUsernameAndCreatedAtAfter(String username, LocalDateTime bankruptAt);

}
