package com.messagingapp.repository;

import com.messagingapp.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, Long> {

    boolean existsByEventId(UUID eventId);
}