// The code is taken from the website:
// https://dev.to/pryhmez/implementing-spring-security-6-with-spring-boot-3-a-guide-to-oauth-and-jwt-with-nimbus-for-authentication-2lhf

package com.ddm.ddm_backend.dto;

public class AuthDto {
    public record LoginRequest(String username, String password) {
    }

    public record Response(String message, String token) {
    }
}
