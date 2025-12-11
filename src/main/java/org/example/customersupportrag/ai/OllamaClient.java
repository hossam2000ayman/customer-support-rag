package org.example.customersupportrag.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@Slf4j
public class OllamaClient {
    private final WebClient.Builder webClientBuilder;

    public OllamaClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Value("${spring.ai.ollama.base-url}")
    private String ollamaBaseUrl;

    @Value("${spring.ai.ollama.chat.model}")
    private String ollamaModel;

    @Value("${spring.ai.ollama.embedding.model}")
    private String ollamaEmbeddingModel;

    private WebClient client() {
        return WebClient.builder()
                .baseUrl(ollamaBaseUrl)
                .build();
    }

    public String generate(String prompt) {
        Map<String, Object> body = Map.of(
                "model", ollamaModel,
                "prompt", prompt,
                "stream", false
        );

        return client().post()
                .uri("/api/generate")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .map(resp -> (String) resp.get("response"))
                .block();
    }

    public double[] embed(String text) {
        Map<String, Object> body = Map.of(
                "model", ollamaEmbeddingModel,
                "input", text
        );

        Map<String, Object> resp = client().post()
                .uri("/api/embeddings")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        var embeddingList = (java.util.List<Number>)
                ((java.util.List<?>) resp.get("embeddings")).get(0);

        return embeddingList.stream().mapToDouble(Number::doubleValue).toArray();
    }
}
