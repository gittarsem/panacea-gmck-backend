package com.panacea.backend.controller;

import com.panacea.backend.Entity.UserEntity;
import com.panacea.backend.Enum.Role;
import com.panacea.backend.dto.User.UserDTO;
import com.panacea.backend.dto.admin.AdminApprovalRequest;
import com.panacea.backend.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Value("${PANACEA_ADMIN_APPROVAL_PASSWORD}")
    private String adminApprovalPassword;

    public AdminController(
            UserRepository userRepository,
            ModelMapper modelMapper
    ) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    @PostMapping("/users/access")
    public ResponseEntity<List<UserDTO>> accessUsers(
            @RequestBody AdminApprovalRequest request
    ) {

        validateApprovalPassword(request);

        List<UserEntity> users =
                userRepository.findAll();

        List<UserDTO> result =
                new ArrayList<>();

        for (UserEntity user : users) {

            if (user.getRoles() != null &&
                    user.getRoles().contains(Role.ROLE_ADMIN)) {
                continue;
            }

            result.add(
                    modelMapper.map(
                            user,
                            UserDTO.class
                    )
            );
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/users/{userId}/approve")
    public ResponseEntity<UserDTO> approveAdmin(
            @PathVariable Long userId,
            @RequestBody AdminApprovalRequest request
    ) {

        validateApprovalPassword(request);

        UserEntity user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User not found"
                                )
                        );

        Set<Role> roles =
                user.getRoles() == null
                        ? new HashSet<>()
                        : new HashSet<>(user.getRoles());

        roles.remove(Role.ROLE_GUEST);
        roles.add(Role.ROLE_ADMIN);

        user.setRoles(roles);

        UserEntity saved =
                userRepository.save(user);

        return ResponseEntity.ok(
                modelMapper.map(
                        saved,
                        UserDTO.class
                )
        );
    }

    private void validateApprovalPassword(
            AdminApprovalRequest request
    ) {

        if (request == null ||
                request.approvalPassword() == null ||
                request.approvalPassword().isBlank()) {

            throw new IllegalArgumentException(
                    "Approval password is required"
            );
        }

        if (!adminApprovalPassword.equals(
                request.approvalPassword()
        )) {

            throw new IllegalArgumentException(
                    "Invalid approval password"
            );
        }
    }
}