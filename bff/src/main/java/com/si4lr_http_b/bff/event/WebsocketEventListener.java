package com.si4lr_http_b.bff.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
@RequiredArgsConstructor
public class WebsocketEventListener {

    private final SimpMessageSendingOperations messageSendingOperations;

    @EventListener
    public void handleWebSocketConnectListener(SessionSubscribeEvent sessionSubscribeEvent) {
        StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.wrap(sessionSubscribeEvent.getMessage());

        // Get session ID
        String sessionId = stompHeaderAccessor.getSessionId();

        System.out.println("User Subscribed - Session ID: " + sessionId);
    }

    /**
     * This method listens for SessionDisconnectEvent, which is triggered when a WebSocket session is disconnected.
     * This can happen when:
     *      -A user closes the browser tab.
     *      -The network connection is lost.
     *      -The server disconnects the session due to inactivity.
     *      -The client explicitly sends a disconnect frame.
     *
     * The annotation @EventListener tells Spring to execute this method when an event of type SessionDisconnectEvent occurs.
     * Whenever a WebSocket session disconnects, Spring raises a SessionDisconnectEvent.
     * Since this method subscribes to this event using @EventListener, Spring automatically calls this method and passes the SessionDisconnectEvent object.
     * */
    @EventListener
    public void handleWebsocketDisconnectListener(SessionDisconnectEvent sessionDisconnectEvent){
        // Wrap the SessionDisconnectEvent message to access STOMP-specific headers
        StompHeaderAccessor stompHeaderAccessor = StompHeaderAccessor.wrap(sessionDisconnectEvent.getMessage());

        // Get session ID
        String sessionId = stompHeaderAccessor.getSessionId();

        System.out.println("User Disconnected - Session ID: " + sessionId);
    }

}
