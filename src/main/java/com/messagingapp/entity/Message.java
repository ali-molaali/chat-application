package com.messagingapp.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private UUID eventId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "room_id", nullable = false, length = 100)
    private String roomId;

    @Column(nullable = false, length = 2000)
    private String text;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    protected Message() {
    }

    public Message(
            UUID eventId,
            Long senderId,
            String roomId,
            String text,
            Instant sentAt
    ) {
        this.eventId = eventId;
        this.senderId = senderId;
        this.roomId = roomId;
        this.text = text;
        this.sentAt = sentAt;
    }

    public Long getId() {
        return id;
    }
}