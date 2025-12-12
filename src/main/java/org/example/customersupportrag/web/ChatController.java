package org.example.customersupportrag.web;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("api/chat/")
public class ChatController {
    private final OllamaChatModel ollamaChatModel;
    private final ChatMemory chatMemory;

    public ChatController(OllamaChatModel ollamaChatModel, ChatMemory chatMemory) {
        this.ollamaChatModel = ollamaChatModel;
        this.chatMemory = chatMemory;
    }


    public record SupportAnswer(String answer, double confidence) {}

    @GetMapping("/{conversationalId}")
    public SupportAnswer chat(@PathVariable String conversationalId, @RequestParam String question) {
        List<Message> historyMessages = new ArrayList<>(
                chatMemory.get(conversationalId).stream()
                        .filter(message -> !(message instanceof SystemMessage))
                        .toList()
        );

        SystemMessage systemMessage = new SystemMessage(
                String.format("""
                        You are a helpful customer support AI agent.
                        Answer clearly and concisely.
                        
                        Question: %s
                        """, question
                )
        );
        historyMessages.add(systemMessage);

        BeanOutputConverter<SupportAnswer> outputConverter = new BeanOutputConverter<>(SupportAnswer.class);
        Prompt prompt = new Prompt(
                historyMessages,
                OllamaChatOptions.builder()
                        .model(OllamaModel.LLAMA3)
                        .format(outputConverter.getJsonSchemaMap())
                        .build()
        );

        ChatResponse response = ollamaChatModel.call(prompt);

        AssistantMessage assistantMessage = response.getResult().getOutput();
        List<Message> messages = prompt.getInstructions();
        messages.add(assistantMessage);
        chatMemory.add(conversationalId, messages);

        String json = assistantMessage.getText();

        return outputConverter.convert(json);
    }

    @GetMapping(value = "/stream/{conversationalId}", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> chatStream(@PathVariable String conversationalId, @RequestParam String question) {
        List<Message> historyMessages = new ArrayList<>(chatMemory.get(conversationalId));

        UserMessage userMessage = new UserMessage(question);
        historyMessages.add(userMessage);

        Prompt prompt = new Prompt(
                historyMessages,
                OllamaChatOptions.builder()
                        .model(OllamaModel.LLAMA3)
                        .build()
        );

        StringBuilder finalAnswerBuffer = new StringBuilder();
        return ollamaChatModel.stream(prompt)
                .mapNotNull(chatResponse -> {
                    String token = chatResponse.getResult().getOutput().getText();
                    finalAnswerBuffer.append(token);
                    return token;
                })
                .doOnComplete(() -> {
                    AssistantMessage assistantMessage = new AssistantMessage(finalAnswerBuffer.toString());
                    historyMessages.add(assistantMessage);

                    chatMemory.add(conversationalId, historyMessages);
                });
    }
}
