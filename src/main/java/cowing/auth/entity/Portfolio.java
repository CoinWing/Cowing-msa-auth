package cowing.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "market_code", nullable = false)
    private String marketCode;

    @Column(name = "quantity", nullable = false, precision = 20, scale = 8)
    private BigDecimal quantity;

    @Column(name = "average_cost", nullable = false)
    private Long averageCost;

    @Column(name = "total_cost", nullable = false)
    private Long totalCost;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
