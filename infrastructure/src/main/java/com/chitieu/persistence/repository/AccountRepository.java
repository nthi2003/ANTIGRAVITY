package com.chitieu.persistence.repository;

import com.chitieu.persistence.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {
    List<AccountEntity> findByUserId(UUID userId);

    @org.springframework.data.jpa.repository.Query("SELECT u.id as userId, u.fullName as fullName, u.username as username, SUM(a.balance) as totalWealth "
            +
            "FROM AccountEntity a JOIN a.user u " +
            "GROUP BY u.id, u.fullName, u.username " +
            "ORDER BY totalWealth DESC")
    List<WealthProjection> findTopWealthyUsers(org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT a.user.id, SUM(a.balance) FROM AccountEntity a WHERE a.user.id IN :userIds GROUP BY a.user.id")
    List<Object[]> findTotalBalanceByUserIds(
            @org.springframework.data.repository.query.Param("userIds") List<UUID> userIds);
}
