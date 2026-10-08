package com.panacea.backend.dto.User;

import com.panacea.backend.Enum.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private Long id;

    private String fullName;

    private String email;

    private String phoneNumber;

    private String collegeName;

    private String city;

    private String state;

    private Set<Role> roles;
}