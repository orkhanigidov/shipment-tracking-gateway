package com.example.gateway.controller;

import com.example.gateway.dto.ChatRequest;
import com.example.gateway.service.AiTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Tag(name = "AI Assistant", description = "Natural language shipment search and conversational tracking assistant")
@SecurityRequirement(name = "bearerAuth")
public class AiController {

    private final AiTrackingService aiTrackingService;

    @Operation(summary = "Ask a natural language question about shipments",
            description = """
                    Performs semantic (RAG) search over indexed shipments using your question.
                    Example: 'Are there any DHL shipments going to Hamburg that are in transit?'
                    Returns an AI-generated answer grounded in your actual shipment data.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Answer returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    })
    @GetMapping("/search")
    public String search(@RequestParam String q) {
        return aiTrackingService.askAboutShipments(q);
    }

    @Operation(summary = "Chat with the shipment assistant",
            description = """
                    Conversational interface with tool calling and persistent memory per user.
                    The assistant can look up live tracking status when asked about a tracking number.
                    Conversation history is stored in PostgreSQL and survives app restarts.
                    Example body: { "message": "Where is my shipment TRK-12345?" }
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Assistant reply returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    })
    @PostMapping("/chat")
    public String chat(@AuthenticationPrincipal String userId, @RequestBody ChatRequest request) {
        return aiTrackingService.chat(userId, request.message());
    }
}
