package com.priyanshu886291kumar.projects.AirBnb_SpringBoot.controllers.Authentication;

import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Authentication.LoginDTO;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Authentication.LoginResponseDTO;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Authentication.SignUpRequestDTO;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.User.UserDTO;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.security.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<UserDTO>signup(@RequestBody SignUpRequestDTO signUpRequestDTO){
        return new ResponseEntity<>(authService.signup(signUpRequestDTO), HttpStatus.CREATED);
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO>login(@RequestBody LoginDTO loginDTO, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){
        String[]tokens = authService.login(loginDTO);
        Cookie cookie = new Cookie("refreshToken", tokens[1]);
        cookie.setHttpOnly(true);
        httpServletResponse.addCookie(cookie);
        return ResponseEntity.ok(new LoginResponseDTO(tokens[0]));


    }
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO>refresh(HttpServletRequest request){
      String refreshToken = Arrays.stream(request.getCookies())
              .filter(cookie->"refreshToken".equals(cookie.getName()))
              .findFirst()
              .map(Cookie::getValue)
              .orElseThrow(()->new AuthenticationServiceException("Refresh token not found inside the cookies"));

      String accessToken =  authService.refreshToken(refreshToken);
      return  ResponseEntity.ok(new LoginResponseDTO(accessToken));

    }


}
