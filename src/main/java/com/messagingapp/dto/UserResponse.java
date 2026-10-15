package com.messagingapp.dto;

import java.time.Instant;
import com.messagingapp.entity.User;

public record UserResponse(Long id, String username, String email, Instant createdAt) {

 public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getCreatedAt());
    }

}
