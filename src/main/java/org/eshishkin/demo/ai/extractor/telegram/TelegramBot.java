package org.eshishkin.demo.ai.extractor.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.Serializable;
//import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramBot implements LongPollingSingleThreadUpdateConsumer {
    TelegramClient telegramClient = new OkHttpTelegramClient("XXXX");
    @EventListener(ContextRefreshedEvent.class)
    public void listen() {
        try {
            String botToken = "XXXX";
            TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();
            botsApplication.registerBot(botToken, this);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void consume(Update update) {
        System.out.println(update);
        if (update.hasMessage() && update.getMessage().hasText()) {
            var user = update.getMessage().getFrom();
            if (!"XXXX".equals(user.getUserName())) {
                return;
            }
            var chatId = update.getMessage().getChatId().toString();
            var test = new BotApiMethod<>() {
                private final String chat_id = "XXXX";

                @Override
                public Serializable deserializeResponse(String answer) throws TelegramApiRequestException {
                    return answer;
                }

                @Override
                public String getMethod() {
                    return "getChat";
                }
            };

            SendMessage sendMessage = new SendMessage(chatId, update.getMessage().getText());
            try {
                // Execute it
                telegramClient.execute(test);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}
