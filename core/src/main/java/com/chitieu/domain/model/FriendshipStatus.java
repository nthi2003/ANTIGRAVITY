package com.chitieu.domain.model;

public enum FriendshipStatus {
    PENDING, // Friend request sent, waiting for response
    ACCEPTED, // Friend request accepted
    BLOCKED, // User blocked
    NONE // No relationship
}
