package com.messagingapp.service;

import com.messagingapp.event.ChatMessageEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class ChatMessageConsumerTest {

    @Mock
    private MessagePersistenceService messagePersistenceService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Test
    void shouldPersistAndBroadcastEventToItsRoom() {
        ChatMessageEvent event = new ChatMessageEvent(
                UUID.randomUUID(),
                1L,
                "general",
                "Hello",
                Instant.parse("2026-11-05T10:00:00Z")
        );
        ChatMessageConsumer consumer = new ChatMessageConsumer(
                messagePersistenceService,
                messagingTemplate
        );

        consumer.consume(event);

        InOrder inOrder = inOrder(messagePersistenceService, messagingTemplate);
        inOrder.verify(messagePersistenceService).save(event);
        inOrder.verify(messagingTemplate)
                .convertAndSend("/topic/rooms/general", event);
        verifyNoMoreInteractions(messagePersistenceService, messagingTemplate);
    }
}
