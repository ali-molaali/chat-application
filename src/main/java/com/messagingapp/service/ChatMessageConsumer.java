package com.messagingapp.service;

import com.messagingapp.event.ChatMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ChatMessageConsumer {

    public ChatMessageConsumer(com.messagingapp.service.MessagePersistenceService messagePersistenceService) {
        this.messagePersistenceService = messagePersistenceService;
    }
    private final MessagePersistenceService messagePersistenceService;
    private static final Logger log =
            LoggerFactory.getLogger(ChatMessageConsumer.class);

    @KafkaListener(topics = "chat-messages")
    public void consume(ChatMessageEvent event) {
        messagePersistenceService.save(event);
        log.info(
                "Received message: eventId={}, roomId={}, senderId={}, text={}",
                event.eventId(),
                event.roomId(),
                event.senderId(),
                event.text()
        );
    }
}