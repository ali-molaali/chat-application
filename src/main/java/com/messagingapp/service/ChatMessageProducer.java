package com.messagingapp.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.messagingapp.event.ChatMessageEvent;

@Service
public class ChatMessageProducer {

    private final  KafkaTemplate<String, ChatMessageEvent> kafkaTemplate;

    public ChatMessageProducer(KafkaTemplate<String, ChatMessageEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<SendResult<String, ChatMessageEvent>> send(
            ChatMessageEvent event
    ) {
        return kafkaTemplate.send(
                "chat-messages",
                event.roomId(),
                event
        );

    }
}
