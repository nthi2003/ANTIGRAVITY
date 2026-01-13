package com.chitieu.domain.repository;

import com.chitieu.domain.model.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {
    Optional<User> findById(UUID id);

    Optional<User> findByUsername(String username);

    List<User> searchUsers(String query);

    List<User> findAll();

    User save(User user);
}
