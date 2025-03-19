package org.eshishkin.demo.ai.chat.backend.web;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class ChatController {
    private final ChatClient chatClient;

    @PostMapping("/chat")
    public String chat(@RequestParam String user, @RequestBody String request) {
        var response = chatClient.prompt()
                .user(request)
                .advisors(spec -> {
                    spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, user);
                })
                .call();

        return response.content();
    }
}
