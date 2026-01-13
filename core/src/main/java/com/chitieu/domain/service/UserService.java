package com.chitieu.domain.service;

import com.chitieu.domain.model.User;
import java.util.List;
import java.util.UUID;

public interface UserService {
    User getProfile(UUID userId);

    User updateProfile(UUID userId, User userUpdate);

    List<User> searchUsers(String query);

    com.chitieu.domain.model.UserStatistics getUserStats(UUID userId);
}
