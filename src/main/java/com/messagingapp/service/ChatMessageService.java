package com.messagingapp.service;

import com.messagingapp.dto.SendMessageRequest;
import com.messagingapp.event.ChatMessageEvent;
import com.messagingapp.exception.UserNotFoundException;
import com.messagingapp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class ChatMessageService {

    private final UserRepository userRepository;
    private final ChatMessageProducer producer;

    public ChatMessageService(
            UserRepository userRepository,
            ChatMessageProducer producer
    ) {
        this.userRepository = userRepository;
        this.producer = producer;
    }

    public CompletableFuture<ChatMessageEvent> send(
            SendMessageRequest request
    ) {
        if (!userRepository.existsById(request.senderId())) {
            throw new UserNotFoundException(request.senderId());
        }

        ChatMessageEvent event = new ChatMessageEvent(
                UUID.randomUUID(),
                request.senderId(),
                request.roomId(),
                request.text(),
                Instant.now()
        );

        return producer.send(event)
                .thenApply(result -> event);
    }
}