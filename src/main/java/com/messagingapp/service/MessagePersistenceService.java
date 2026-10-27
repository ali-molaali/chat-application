package com.messagingapp.service;

import com.messagingapp.entity.Message;
import com.messagingapp.event.ChatMessageEvent;
import com.messagingapp.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessagePersistenceService {

    private final MessageRepository messageRepository;

    public MessagePersistenceService(
            MessageRepository messageRepository
    ) {
        this.messageRepository = messageRepository;
    }

    @Transactional
    public void save(ChatMessageEvent event) {
        if (messageRepository.existsByEventId(event.eventId())) {
            return;
        }

        Message message = new Message(
                event.eventId(),
                event.senderId(),
                event.roomId(),
                event.text(),
                event.sentAt()
        );

        messageRepository.save(message);
    }
}