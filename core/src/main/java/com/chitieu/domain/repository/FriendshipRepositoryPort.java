package com.chitieu.domain.repository;

import com.chitieu.domain.model.Friendship;
import com.chitieu.domain.model.FriendshipStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendshipRepositoryPort {

    Friendship save(Friendship friendship);

    Optional<Friendship> findById(UUID id);

    Optional<Friendship> findByUserIds(UUID userId, UUID friendId);

    List<Friendship> findByUserId(UUID userId);

    List<Friendship> findByUserIdAndStatus(UUID userId, FriendshipStatus status);

    List<Friendship> findAcceptedFriends(UUID userId);

    List<Friendship> findPendingRequests(UUID userId);

    List<Friendship> findSentRequests(UUID userId);

    boolean existsByUserIds(UUID userId, UUID friendId);

    void delete(UUID id);

    List<Friendship> findAllByUserId(UUID userId);

    void deleteByUserIds(UUID userId, UUID friendId);
}
