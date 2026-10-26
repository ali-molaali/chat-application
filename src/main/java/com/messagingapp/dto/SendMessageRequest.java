package com.messagingapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(
        @NotNull
        @Positive
        Long senderId,
        @NotBlank
        @Size(max = 100)
        String roomId,
        @NotBlank
        @Size(max = 2000)
        String text
        ) {

}
