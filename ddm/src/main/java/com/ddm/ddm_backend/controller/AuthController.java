// The code is taken from the website:
// https://dev.to/pryhmez/implementing-spring-security-6-with-spring-boot-3-a-guide-to-oauth-and-jwt-with-nimbus-for-authentication-2lhf

package com.ddm.ddm_backend.controller;

import com.ddm.ddm_backend.dto.AuthDTO;
import com.ddm.ddm_backend.dto.RegistrationDTO;
import com.ddm.ddm_backend.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDTO.LoginRequest userLogin) throws IllegalAccessException {
        return authService.login(userLogin);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistrationDTO userRegister) {
        return authService.register(userRegister);
    }
}
