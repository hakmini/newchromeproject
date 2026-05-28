package com.example.newchromeproject.WebSocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer { //STOMP 프로토콜을 사용하여 publish/subscribe 구조를 쉽게 구축

    public void configureMessageBroker(MessageBrokerRegistry config){ //메시지 브로커 설정
        config.enableSimpleBroker("/topic"); // 서->클
        config.setApplicationDestinationPrefixes("/app"); // 클->서
    }

    public void registerStompEndpoints(StompEndpointRegistry registry){
        registry
                .addEndpoint("/coinprice") //서버 접속 endpoint
                .setAllowedOriginPatterns("*") //http://localhost:3000
                .withSockJS();
    }
}
