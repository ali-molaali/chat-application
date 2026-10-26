package com.messagingapp.controller;

import com.messagingapp.dto.SendMessageRequest;
import com.messagingapp.event.ChatMessageEvent;
import com.messagingapp.service.ChatMessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/messages")
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    public ChatMessageController(ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public CompletableFuture<ChatMessageEvent> send(
            @Valid @RequestBody SendMessageRequest request
    ) {
        return chatMessageService.send(request);
    }
}