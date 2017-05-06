package com.arpit;

import com.hazelcast.config.Config;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer 
{
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry)
	{
		registry.addHandler(sessionHandler(), "/chat");
	}

	@Bean
	public NewSessionHandler sessionHandler()
	{
        NewSessionHandler handler = new NewSessionHandler();
        handler.setConnection();
        return handler;
	}


}