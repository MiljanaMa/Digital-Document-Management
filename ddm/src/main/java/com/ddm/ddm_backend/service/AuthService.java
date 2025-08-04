// The code is taken from the website:
// https://dev.to/pryhmez/implementing-spring-security-6-with-spring-boot-3-a-guide-to-oauth-and-jwt-with-nimbus-for-authentication-2lhf

package com.ddm.ddm_backend.service;

import com.ddm.ddm_backend.dto.AuthDto;
import com.ddm.ddm_backend.dto.RegistrationDto;
import com.ddm.ddm_backend.mapper.DtoUtils;
import com.ddm.ddm_backend.model.User;
import com.ddm.ddm_backend.repository.RoleRepository;
import com.ddm.ddm_backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    @Autowired
    private JwtEncoder jwtEncoder;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    private AuthenticationManager authenticationManager;


    public String generateToken(Authentication authentication) {
        Instant now = Instant.now();

        String scope = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(10, ChronoUnit.HOURS))
                .subject(authentication.getName())
                .claim("scope", scope)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public ResponseEntity<AuthDto.Response> login(AuthDto.LoginRequest userLogin){
        try{
            Authentication authentication =
                    authenticationManager
                            .authenticate(new UsernamePasswordAuthenticationToken(
                                    userLogin.username(),
                                    userLogin.password()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = (User) authentication.getPrincipal();
            log.info("User role: {}", user.getRole().getName());
            log.info("Token requested for user :{}", authentication.getAuthorities());
            String token = generateToken(authentication);

            AuthDto.Response response = new AuthDto.Response("User logged in successfully", token);
            return new ResponseEntity<AuthDto.Response>(response, HttpStatus.OK);
        }catch (Exception e){
            AuthDto.Response response = new AuthDto.Response("Login error", null);
            return new ResponseEntity<AuthDto.Response>(response, HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<String> register(RegistrationDto registrationDto){
        try {
            User user = (User) new DtoUtils().convertToEntity(new User(), registrationDto);
            Optional<User> foundUser = userRepository.findByUsername(user.getUsername());
            if(foundUser.isPresent()) {
                return new ResponseEntity<>("User with this credentials already exists", HttpStatus.BAD_REQUEST);
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRole(roleRepository.findByName("ROLE_USER"));
            User registeredUser = userRepository.save(user);
            log.info("Registration DTO: {}", registeredUser.getUsername());
            return new ResponseEntity<String>("User registered successfully", HttpStatus.CREATED);
        }catch (Exception e){
            return new ResponseEntity<String>("Registration error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}