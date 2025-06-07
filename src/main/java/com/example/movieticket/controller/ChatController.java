package com.example.movieticket.controller;

import com.example.movieticket.dto.request.ChatRequest;
import com.example.movieticket.dto.response.ChatResponse;
import com.example.movieticket.dto.response.ResponseData;
import com.example.movieticket.service.impl.CohereService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatController {

    CohereService cohereService;

    @PostMapping
    public ResponseData<ChatResponse> chat(@RequestBody ChatRequest request) {
        try {
            String reply = cohereService.ask(request.getQuestion());

            return ResponseData.<ChatResponse>builder()
                    .code(200)
                    .message("Success")
                    .data(ChatResponse.builder()
                            .answer(reply)
                            .success(true)
                            .build())
                    .build();

        } catch (Exception e) {
            return ResponseData.<ChatResponse>builder()
                    .code(500)
                    .message("Error")
                    .data(ChatResponse.builder()
                            .success(false)
                            .error(e.getMessage())
                            .build())
                    .build();
        }
    }
}
