package com.chitieu.domain.service;

import com.chitieu.domain.model.User;
import com.chitieu.domain.repository.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepositoryPort userRepository;
    private final com.chitieu.domain.repository.TransactionRepositoryPort transactionRepository;
    private final com.chitieu.domain.repository.FriendshipRepositoryPort friendshipRepository;
    private final com.chitieu.domain.repository.GoalRepositoryPort goalRepository;

    @Override
    public User getProfile(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User updateProfile(UUID userId, User userUpdate) {
        User existingUser = getProfile(userId);

        // Update allowed fields
        if (userUpdate.getFullName() != null) {
            existingUser.setFullName(userUpdate.getFullName());
        }
        if (userUpdate.getEmail() != null) {
            existingUser.setEmail(userUpdate.getEmail());
        }

        return userRepository.save(existingUser);
    }

    @Override
    public List<User> searchUsers(String query) {
        if (query == null || query.trim().isEmpty() || query.length() < 2) {
            return java.util.Collections.emptyList();
        }
        return userRepository.searchUsers(query);
    }

    @Override
    public com.chitieu.domain.model.UserStatistics getUserStats(UUID userId) {
        long transactionCount = transactionRepository.countByUserId(userId);
        long friendCount = friendshipRepository.findAcceptedFriends(userId).size();
        long goalCount = goalRepository.findByUserId(userId).size();

        return com.chitieu.domain.model.UserStatistics.builder()
                .transactionCount(transactionCount)
                .friendCount(friendCount)
                .goalCount(goalCount)
                .build();
    }
}
