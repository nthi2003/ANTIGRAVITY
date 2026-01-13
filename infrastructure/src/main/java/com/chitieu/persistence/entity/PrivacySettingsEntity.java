package com.chitieu.persistence.entity;

import com.chitieu.domain.model.Visibility;
import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "privacy_settings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivacySettingsEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_visibility", nullable = false)
    private Visibility profileVisibility;

    @Enumerated(EnumType.STRING)
    @Column(name = "goals_visibility", nullable = false)
    private Visibility goalsVisibility;

    @Enumerated(EnumType.STRING)
    @Column(name = "transactions_visibility", nullable = false)
    private Visibility transactionsVisibility;

    @Column(name = "allow_friend_requests", nullable = false)
    private boolean allowFriendRequests;

    @Column(name = "show_online_status", nullable = false)
    private boolean showOnlineStatus;
}
