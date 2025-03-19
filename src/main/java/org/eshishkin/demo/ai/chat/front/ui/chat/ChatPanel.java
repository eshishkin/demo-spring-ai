package org.eshishkin.demo.ai.chat.front.ui.chat;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import org.eshishkin.demo.ai.chat.backend.persistence.entity.ChatHistoryEntity;
import org.eshishkin.demo.ai.chat.backend.service.ChatService;
import org.springframework.ai.chat.messages.MessageType;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class ChatPanel extends VerticalLayout {
    private static final String NAME_FOR_USER = "You";
    private static final String NAME_FOR_BOT = "Bot";
    private static final String NAME_FOR_SYSTEM = "System";

    private final ChatService chat;
    private final String chatId;
    private final List<MessageListItem> data = new ArrayList<>();
    private final MessageList container = new MessageList(data);
    private final MessageInput input = new MessageInput();
    private final ProgressBar progress = new ProgressBar();


    public ChatPanel(String chatId, ChatService chat) {
        this.chat = chat;
        this.chatId = chatId;

        var layout = new SplitLayout();
        layout.setWidthFull();
        layout.setHeightFull();
        layout.setSplitterPosition(90);
        layout.setOrientation(SplitLayout.Orientation.VERTICAL);

        layout.addToPrimary(container, progress);
        layout.addToSecondary(input());

        add(layout);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        data.addAll(chat.getChatHistory(chatId).stream().map(this::getMessageListItem).toList());
        container.setItems(data);
    }

    private VerticalLayout input() {
        var layout = new VerticalLayout();
        layout.setHeightFull();

        progress.setIndeterminate(true);
        progress.setVisible(false);

        input.setWidthFull();
        input.addSubmitListener(e -> {
            input.setEnabled(false);
            progress.setVisible(true);
            appendMessageItem(new MessageListItem(e.getValue(), Instant.now(), NAME_FOR_USER));

            chat.ask(chatId, e.getValue()).subscribe(response -> {
                e.getSource().getUI().ifPresent(ui -> ui.access(() -> {
                    appendMessageItem(new MessageListItem(response, Instant.now(), NAME_FOR_SYSTEM));
                    input.setEnabled(true);
                    progress.setVisible(false);
                }));
            });
        });


        layout.add(input);

        return layout;
    }

    private MessageListItem getMessageListItem(ChatHistoryEntity m) {
        var time = m.getTime().toInstant(OffsetDateTime.now().getOffset());
        var author = switch (MessageType.fromValue(m.getType())) {
            case MessageType.USER -> NAME_FOR_USER;
            case MessageType.ASSISTANT -> NAME_FOR_BOT;
            default -> NAME_FOR_SYSTEM;
        };
        return new MessageListItem(m.getMessage(), time, m.getType(), author);
    }

    private void appendMessageItem(MessageListItem item) {
        data.add(item);
        container.setItems(data);
    }
}
