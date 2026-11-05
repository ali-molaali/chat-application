package com.messagingapp.service;

import com.messagingapp.event.ChatMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChatMessageConsumer {

    private final MessagePersistenceService messagePersistenceService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatMessageConsumer(
        MessagePersistenceService messagePersistenceService,
        SimpMessagingTemplate messagingTemplate) {
        this.messagePersistenceService = messagePersistenceService;
    this.messagingTemplate = messagingTemplate;
    }

    private static final Logger log =
            LoggerFactory.getLogger(ChatMessageConsumer.class);

    @KafkaListener(topics = "chat-messages")
    public void consume(ChatMessageEvent event) {
        messagePersistenceService.save(event);
    messagingTemplate.convertAndSend(
        "/topic/rooms/" + event.roomId(),
        event
    );
        log.info(
                "Received message: eventId={}, roomId={}, senderId={}, text={}",
                event.eventId(),
                event.roomId(),
                event.senderId(),
                event.text()
        );
    }
}