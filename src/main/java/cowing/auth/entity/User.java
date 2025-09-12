package cowing.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user", indexes = {@Index(name = "username_idx", columnList = "username", unique = true)})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "u_id")
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwd;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private Authority authority;

    @Column(precision = 20, scale = 8, nullable = true)
    private Long uHoldings;

    @Column
    private LocalDateTime bankruptAt;

    @Column
    private LocalDateTime deletedAt;

    @PrePersist
    public void prePersist() {
            this.authority = Authority.ROLE_USER; // 기본 권한 설정
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public void setUHoldings(Long uHoldings) {
        this.uHoldings = uHoldings;
    }
    public void markAsBankrupt() {this.bankruptAt = LocalDateTime.now();}
    public void markAsDeleted() {this.deletedAt = LocalDateTime.now();}


}