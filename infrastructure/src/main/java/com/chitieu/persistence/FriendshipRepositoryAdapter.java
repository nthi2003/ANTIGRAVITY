package com.chitieu.persistence;

import com.chitieu.domain.model.Friendship;
import com.chitieu.domain.model.FriendshipStatus;
import com.chitieu.domain.repository.FriendshipRepositoryPort;
import com.chitieu.persistence.entity.FriendshipEntity;
import com.chitieu.persistence.repository.FriendshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FriendshipRepositoryAdapter implements FriendshipRepositoryPort {

    private final FriendshipRepository friendshipRepository;

    @Override
    public Friendship save(Friendship friendship) {
        FriendshipEntity entity = toEntity(friendship);
        return toDomain(friendshipRepository.save(entity));
    }

    @Override
    public Optional<Friendship> findById(UUID id) {
        return friendshipRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Friendship> findByUserIds(UUID userId, UUID friendId) {
        return friendshipRepository.findByParticipants(userId, friendId).map(this::toDomain);
    }

    @Override
    public List<Friendship> findByUserId(UUID userId) {
        return friendshipRepository.findByUserId(userId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Friendship> findByUserIdAndStatus(UUID userId, FriendshipStatus status) {
        return friendshipRepository.findByUserIdAndStatus(userId, status).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Friendship> findAcceptedFriends(UUID userId) {
        return findByUserIdAndStatus(userId, FriendshipStatus.ACCEPTED);
    }

    @Override
    public List<Friendship> findPendingRequests(UUID userId) {
        return friendshipRepository.findIncomingRequests(userId, FriendshipStatus.PENDING).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Friendship> findSentRequests(UUID userId) {
        return friendshipRepository.findByRequestedByAndStatus(userId, FriendshipStatus.PENDING).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByUserIds(UUID userId, UUID friendId) {
        return friendshipRepository.existsByParticipants(userId, friendId);
    }

    @Override
    public void delete(UUID id) {
        friendshipRepository.deleteById(id);
    }

    @Override
    public List<Friendship> findAllByUserId(UUID userId) {
        return findByUserId(userId);
    }

    @Override
    public void deleteByUserIds(UUID userId, UUID friendId) {
        // Find specifically the relationship (assuming bidirectional handling in repo)
        // JPA delete requires a transaction usually, or we can find then delete.
        // Repository has `deleteByUserIdAndFriendId` but that matches exact columns.
        // We should find the entity then delete it to be safe with "participants"
        // logic.
        friendshipRepository.findByParticipants(userId, friendId).ifPresent(friendshipRepository::delete);
    }

    private Friendship toDomain(FriendshipEntity entity) {
        return Friendship.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .friendId(entity.getFriendId())
                .status(entity.getStatus())
                .requestedBy(entity.getRequestedBy())
                .requestedAt(entity.getRequestedAt())
                .acceptedAt(entity.getAcceptedAt())
                .build();
    }

    private FriendshipEntity toEntity(Friendship domain) {
        return FriendshipEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .friendId(domain.getFriendId())
                .status(domain.getStatus())
                .requestedBy(domain.getRequestedBy())
                .requestedAt(domain.getRequestedAt())
                .acceptedAt(domain.getAcceptedAt())
                .build();
    }
}
