package com.chitieu.persistence.repository;

import com.chitieu.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    java.util.List<UserEntity> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String usernameQuery,
            String emailQuery);
}
