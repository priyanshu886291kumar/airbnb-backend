package com.priyanshu886291kumar.projects.AirBnb_SpringBoot.security;

import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Authentication.LoginDTO;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.Authentication.SignUpRequestDTO;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.dto.User.UserDTO;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.Users.User;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.entity.enums.UserRole;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.exception.ResourceNotFoundException;
import com.priyanshu886291kumar.projects.AirBnb_SpringBoot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private  final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    public UserDTO signup( SignUpRequestDTO signUpRequestDTO){
        User user = userRepository.findByEmail(signUpRequestDTO.getEmail()).orElse(null);
        if(user!=null){
            throw  new RuntimeException("User already present with email: "+signUpRequestDTO.getEmail());
        }
        User newUser = modelMapper.map(signUpRequestDTO,User.class);
        newUser.setRoles(Set.of(UserRole.GUEST));
        newUser.setPassword(passwordEncoder.encode(signUpRequestDTO.getPassword()));
        newUser = userRepository.save(newUser);
        return modelMapper.map(newUser,UserDTO.class);
    }
    public String[]login(LoginDTO loginDTO){
       Authentication authentication =  authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getEmail(),loginDTO.getPassword()));
       User user = (User)authentication.getPrincipal();
       String arr[] = new String[2];
       arr[0] = jwtService.generateAccessToken(user);
       arr[1] = jwtService.generateRefreshToken(user);
       return arr;
    }

    public String refreshToken(String refreshToken){
        Long id = jwtService.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("User not found with id: "+id));
        return jwtService.generateAccessToken(user);
    }
}
