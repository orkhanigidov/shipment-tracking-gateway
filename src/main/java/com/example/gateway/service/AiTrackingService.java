package com.example.gateway.service;

import com.example.gateway.dto.TrackingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiTrackingService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final JdbcChatMemoryRepository memoryRepository;
    private final LiveTrackingService liveTrackingService;

    /**
     * RAG endpoint - user asks a natural language question.
     * We embed the question, retrieve the top 5 semantically similar
     * shipment documents from Elasticsearch, and send them as context to the LLM.
     * <p>
     * Example: "Are there any shipments going to Hamburg that are delayed?"
     */
    public String askAboutShipments(String question) {
        // 1. Semantic similarity search in the vector store
        List<Document> relevant = vectorStore.similaritySearch(SearchRequest.builder().query(question).topK(5).build());

        if (relevant.isEmpty()) {
            return "No shipments found matching your query.";
        }

        // 2. Build context block from retrieved documents
        String context = relevant.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        log.debug("RAG context for question '{}': {} documents retrieved", question, relevant.size());

        // 3. Send question + context to LLM
        return chatClient.prompt()
                .user(u -> u.text("""
                                Answer the following question using only the shipment data provided below.
                                Do not make up tracking numbers or statuses that are not in the data.
                                
                                Shipment data:
                                {context}
                                
                                Question: {question}
                                """)
                        .param("context", context)
                        .param("question", question))
                .call()
                .content();
    }

    /**
     * Conversational chat with tool calling + persistent memory per user.
     * The LLM decides on its own when to call getShipmentStatus() - you
     * never hardcode "if the user mentions a tracking number, call this".
     * <p>
     * Example flow:
     * User: "Where is TRK-12345?"
     * -> LLM calls getShipmentStatus("TRK-12345") automatically
     * -> LLM formats the result into a human reply
     * <p>
     * User (follow-up): "What about the one to Hamburg?"
     * -> Memory provides context, LLM infers the correct tracking number
     */
    public String chat(String userId, String userMessage) {
        // Per-user memory window - keeps last 20 messages in Postgres
        ChatMemory memory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(memoryRepository)
                .maxMessages(20)
                .build();

        return chatClient.prompt()
                .user(userMessage)
                .tools(this)
                .advisors(MessageChatMemoryAdvisor.builder(memory).build())
                .call()
                .content();
    }

    /**
     * Tool definition - the LLM calls this when it needs live shipment data.
     * Spring AI reads the @Tool annotation and passes the description to the model
     * so it knows when and how to call this method.
     */
    @Tool(description = """
            Retrieves the current live tracking status for a shipment.
            Use this when the user asks about a specific tracking number.
            Returns carrier, status, current location, and estimated delivery date.
            """)
    public String getShipmentStatus(String trackingNumber) {
        try {
            TrackingResponse resp = liveTrackingService.getTracking(trackingNumber);
            return String.format(
                    "Shipment %s (carrier: %s) is currently %s at %s. Estimated delivery: %s.",
                    resp.trackingNumber(),
                    resp.carrier(),
                    resp.status(),
                    resp.currentLocation(),
                    resp.estimatedDelivery()
            );
        } catch (ShipmentNotFoundException e) {
            return "Shipment with tracking number " + trackingNumber + " was not found.";
        }
    }
}
