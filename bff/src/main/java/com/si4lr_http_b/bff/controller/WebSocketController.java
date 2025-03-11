package com.si4lr_http_b.bff.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class WebSocketController {
    private final SimpMessageSendingOperations messagingTemplate;

    @SendTo("/topic/updates")
    public String subscribe() {
        return "Subscribed to real-time updates!";
    }

    public void sendUpdate(String taskId) {
        Map<String, Object> message = new HashMap<>();
        message.put("taskId", taskId);
        message.put("status", "COMPLETED");

        messagingTemplate.convertAndSend("/topic/updates", message);
    }
}
