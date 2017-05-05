package com.arpit;

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
		SessionHandler handler =  sessionHandler();
		handler.setConnection();
		registry.addHandler(sessionHandler(), "/chat");
	}

	@Bean
	public SessionHandler sessionHandler()
	{
		return new SessionHandler();
	}

}