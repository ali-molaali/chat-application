package com.messagingapp.service;

import com.messagingapp.event.ChatMessageEvent;
import com.messagingapp.repository.MessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.messagingapp.entity.Message;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MessagePersistenceServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private MessagePersistenceService service;

    @Test
    void shouldSkipAlreadyStoredEvent() {
        ChatMessageEvent event = new ChatMessageEvent(
                UUID.randomUUID(),
                1L,
                "general",
                "Hello",
                Instant.now()
        );

        when(messageRepository.existsByEventId(event.eventId()))
                .thenReturn(true);

        service.save(event);

        verify(messageRepository, never()).save(any());
    }

    @Test
void shouldSaveNewEvent() {
    ChatMessageEvent event = new ChatMessageEvent(
            UUID.randomUUID(),
            1L,
            "general",
            "Hello",
            Instant.parse("2026-10-27T10:00:00Z")
    );

    when(messageRepository.existsByEventId(event.eventId()))
            .thenReturn(false);

    service.save(event);

    ArgumentCaptor<Message> captor =
            ArgumentCaptor.forClass(Message.class);

    verify(messageRepository).save(captor.capture());

    Message expected = new Message(
            event.eventId(),
            event.senderId(),
            event.roomId(),
            event.text(),
            event.sentAt()
    );

    assertThat(captor.getValue())
            .usingRecursiveComparison()
            .isEqualTo(expected);
}
}