package org.example.customersupportrag.web;

import org.example.customersupportrag.ai.OllamaClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/ollama/")
public class OllamaTestController {
    private final OllamaClient ollama;

    public OllamaTestController(OllamaClient ollama) {
        this.ollama = ollama;
    }

    @GetMapping("/generate")
    public String testGenerate(@RequestParam String prompt) {
        return ollama.generate(prompt);
    }

    @GetMapping("/embed")
    public int embed(@RequestParam String text) {
        double[] vector = ollama.embed(text);
        return vector.length; // return vector size for now
    }
}
