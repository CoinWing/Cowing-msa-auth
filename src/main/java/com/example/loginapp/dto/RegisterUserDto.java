package com.example.loginapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterUserDto(
        @JsonProperty("email") @NotBlank @Email String email,
        @JsonProperty("passwd") @NotBlank String passwd,
        @JsonProperty("nickname") @NotBlank String nickname
) {
}