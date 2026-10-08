package com.panacea.backend.dto.login;

import com.panacea.backend.dto.User.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDTO {

    private String accessToken;

    private UserDTO user;

}
