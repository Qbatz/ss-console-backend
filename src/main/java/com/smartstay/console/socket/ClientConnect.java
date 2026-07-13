package com.smartstay.console.socket;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.concurrent.ExecutionException;

@Component
public class ClientConnect {

    @Value("${ENVIRONMENT}")
    private String environment;

    public void connect() throws ExecutionException, InterruptedException {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new JacksonJsonMessageConverter());

        String baseUrl = "wss://ssconsoledevapi.qbatz.com/ws";
        if (environment.equalsIgnoreCase("PROD")) {
            baseUrl = "wss://ssconsoleapi.qbatz.com/ws";
        }

//        "http://localhost:8080/ws"
        StompSession session = stompClient.connectAsync(
                baseUrl,
                new StompSessionHandlerAdapter() {
                    @Override
                    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                        System.out.println("✅ Connected to WebSocket");
                    }

                    @Override
                    public void handleException(StompSession session, StompCommand command,
                                                StompHeaders headers, byte[] payload, Throwable exception) {
                        System.out.println("❌ Exception: " + exception.getMessage());
                    }

                    @Override
                    public void handleTransportError(StompSession session, Throwable exception) {
                        System.out.println("❌ Transport Error: " + exception.getMessage());
                    }
                }
        ).get();

        session.subscribe("admin/online/users", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, @Nullable Object payload) {
                System.out.println("handling frames");
            }
        });
    }
}
