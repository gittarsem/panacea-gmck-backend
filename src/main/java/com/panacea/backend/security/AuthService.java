package com.panacea.backend.security;

import com.panacea.backend.Entity.UserEntity;
import com.panacea.backend.Entity.UserPrincipal;
import com.panacea.backend.Enum.Role;
import com.panacea.backend.Exceptions.ResourceNotFoundException;
import com.panacea.backend.dto.User.LoginResultDTO;
import com.panacea.backend.dto.User.UserDTO;
import com.panacea.backend.dto.login.LoginRequestDTO;
import com.panacea.backend.dto.login.SignUpRequestDTO;
import com.panacea.backend.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

import static com.panacea.backend.utils.AppUtils.giveMeCurrentUser;

@Service
@AllArgsConstructor
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    private final ModelMapper modelMapper;

    private PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    public UserDTO signup(
            SignUpRequestDTO signUpRequestDTO
    ) {

        UserEntity user =
                userRepository
                        .findByEmail(
                                signUpRequestDTO.getEmail()
                        )
                        .orElse(null);

        if (user != null) {
            throw new RuntimeException(
                    "User with same email Id exist"
            );
        }

        UserEntity newUser =
                modelMapper.map(
                        signUpRequestDTO,
                        UserEntity.class
                );

        newUser.setRoles(
                Set.of(Role.ROLE_GUEST)
        );

        newUser.setPassword(
                passwordEncoder.encode(
                        signUpRequestDTO.getPassword()
                )
        );

        userRepository.save(newUser);

        return modelMapper.map(
                newUser,
                UserDTO.class
        );
    }

    public LoginResultDTO login(
            LoginRequestDTO loginRequestDTO
    ) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequestDTO.getEmail(),
                                loginRequestDTO.getPassword()
                        )
                );

        UserPrincipal userPrincipal =
                (UserPrincipal)
                        authentication.getPrincipal();

        UserDTO user =
                modelMapper.map(
                        userPrincipal.getUser(),
                        UserDTO.class
                );

        return new LoginResultDTO(
                jwtService.generateAccessToken(
                        user.getEmail(),
                        user.getRoles()
                ),
                jwtService.generateRefreshToken(
                        user.getEmail()
                ),
                user
        );
    }

    public String refreshToken(
            String refreshToken
    ) {

        String email =
                jwtService.getUserEmailFromToken(
                        refreshToken
                );

        UserEntity user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "User not found with id: "
                                                        + email
                                        )
                        );

        return jwtService.generateAccessToken(
                user.getEmail(),
                user.getRoles()
        );
    }

    public UserDTO getCurrentUser() {

        UserEntity user =
                giveMeCurrentUser();

        return modelMapper.map(
                user,
                UserDTO.class
        );
    }
}