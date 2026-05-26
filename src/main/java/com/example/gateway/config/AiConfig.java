package com.example.gateway.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem("""
                        You are a helpful shipment tracking assistant for a logistics gateway API.
                        You help users track shipments, understand delivery statuses, and find shipments.
                        Answer only about shipments. Decline unrelated questions politely.
                        Keep answers short and factual.
                        """)
                .build();
    }
}
