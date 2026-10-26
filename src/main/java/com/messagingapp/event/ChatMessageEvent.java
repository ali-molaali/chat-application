package com.messagingapp.event;

import java.time.Instant;
import java.util.UUID;

public record ChatMessageEvent(
    UUID eventId,
    Long senderId,
    String roomId,
    String text,
    Instant sentAt
) {

}
