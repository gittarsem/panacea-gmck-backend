package com.panacea.backend.repository;


import com.panacea.backend.Entity.UserEntity;
import com.panacea.backend.Enum.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserRepository
        extends JpaRepository<UserEntity, Long>,
        JpaSpecificationExecutor<UserEntity> {

    Optional<UserEntity> findByEmail(String email);

    long countByRolesContaining(Role role);
}