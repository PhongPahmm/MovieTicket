package com.example.movieticket.service.impl;

import com.cohere.api.Cohere;
import com.cohere.api.requests.ChatRequest;
import com.cohere.api.types.NonStreamedChatResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CohereService {

    Cohere cohere;
    ExcelService excelService;

    public CohereService(@Value("${cohere.api-key}") String apiKey, ExcelService excelService) {
        this.cohere = Cohere.builder()
                .token(apiKey)
                .clientName("movie-ticket-bot")
                .build();
        this.excelService = excelService;
    }

    public String ask(String prompt) {
        String lowerPrompt = prompt.trim().toLowerCase();
        Map<String, String> faqMap = excelService.loadFAQs();

        String bestAnswer = null;
        int maxCommonTokens = 0;

        for (Map.Entry<String, String> entry : faqMap.entrySet()) {
            String faqQuestion = entry.getKey();
            int commonTokens = countCommonTokens(faqQuestion, lowerPrompt);
            if (commonTokens > maxCommonTokens) {
                maxCommonTokens = commonTokens;
                bestAnswer = entry.getValue();
            }
        }

        if (bestAnswer != null && maxCommonTokens > 0) {
            return paraphrase(bestAnswer);
        }
        return ask(prompt, null);
    }


    private int countCommonTokens(String s1, String s2) {
        Set<String> tokens1 = new HashSet<>(Arrays.asList(s1.split("\\s+")));
        Set<String> tokens2 = new HashSet<>(Arrays.asList(s2.split("\\s+")));
        tokens1.retainAll(tokens2);
        return tokens1.size();
    }


    public String ask(String prompt, List<com.cohere.api.types.ChatMessage> chatHistory) {
        ChatRequest chatRequest = ChatRequest.builder()
                .message(prompt)
                .model("command-nightly")
                .maxTokens(150)
                .temperature(0.5f)
                .build();

        NonStreamedChatResponse response = cohere.chat(chatRequest);

        if (response != null) {
            try {
                return (String) response.getClass().getMethod("getMessage").invoke(response);
            } catch (Exception e) {
                return response.toString();
            }
        } else {
            return "Không có phản hồi từ chatbot.";
        }
    }

    private String paraphrase(String answer) {
        String rephrasePrompt = "Hãy diễn đạt lại câu sau sao cho tự nhiên nhưng không thay đổi ý: " + answer;

        ChatRequest chatRequest = ChatRequest.builder()
                .message(rephrasePrompt)
                .model("command-nightly")
                .maxTokens(100)
                .temperature(0.7f)
                .build();

        NonStreamedChatResponse response = cohere.chat(chatRequest);

        if (response != null) {
            try {
                return (String) response.getClass().getMethod("getMessage").invoke(response);
            } catch (Exception e) {
                return answer;
            }
        }
        return answer;
    }
}
