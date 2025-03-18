package com.siron.authenticationservice.dto;

import com.siron.authenticationservice.model.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDto {
    private String accessToken;
    private User user;
}
