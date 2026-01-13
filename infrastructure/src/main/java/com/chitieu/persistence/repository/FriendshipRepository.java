package com.chitieu.persistence.repository;

import com.chitieu.domain.model.FriendshipStatus;

import com.chitieu.persistence.entity.FriendshipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FriendshipRepository extends JpaRepository<FriendshipEntity, UUID> {

    Optional<FriendshipEntity> findByUserIdAndFriendId(UUID userId, UUID friendId);

    @Query("SELECT f FROM FriendshipEntity f WHERE (f.userId = :userId AND f.friendId = :friendId) OR (f.userId = :friendId AND f.friendId = :userId)")
    Optional<FriendshipEntity> findByParticipants(@Param("userId") UUID userId, @Param("friendId") UUID friendId);

    @Query("SELECT f FROM FriendshipEntity f WHERE f.userId = :userId OR f.friendId = :userId")
    List<FriendshipEntity> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT f FROM FriendshipEntity f WHERE (f.userId = :userId OR f.friendId = :userId) AND f.status = :status")
    List<FriendshipEntity> findByUserIdAndStatus(@Param("userId") UUID userId,
            @Param("status") FriendshipStatus status);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM FriendshipEntity f WHERE (f.userId = :userId AND f.friendId = :friendId) OR (f.userId = :friendId AND f.friendId = :userId)")
    boolean existsByParticipants(@Param("userId") UUID userId, @Param("friendId") UUID friendId);

    void deleteByUserIdAndFriendId(UUID userId, UUID friendId);

    // Incoming requests: User is involved, but did not request it, and status is
    // PENDING (passed as param)
    @Query("SELECT f FROM FriendshipEntity f WHERE (f.userId = :userId OR f.friendId = :userId) AND f.requestedBy <> :userId AND f.status = :status")
    List<FriendshipEntity> findIncomingRequests(@Param("userId") UUID userId, @Param("status") FriendshipStatus status);

    // Sent requests: User requesting, status PENDING (passed as param)
    List<FriendshipEntity> findByRequestedByAndStatus(UUID requestedBy, FriendshipStatus status);
}
