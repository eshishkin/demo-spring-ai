package org.eshishkin.demo.ai.chat.front.ui.chat;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.dom.Style;
import org.eshishkin.demo.ai.chat.backend.persistence.entity.ChatHistoryEntity;
import org.eshishkin.demo.ai.chat.backend.service.ChatService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static java.util.Comparator.comparing;
import static org.apache.commons.lang3.StringUtils.left;

public class ChatHistoryPanel extends VerticalLayout {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final String selectedConversationId;

    public ChatHistoryPanel(String chatId, ChatService chat) {
        this.selectedConversationId = chatId;
        setHeightFull();

        add(newChatButton());
        add(new Hr());

        chat.getChats().entrySet().stream().sorted(comparing(e -> e.getKey())).forEach(e -> {
            add(chatGroupComponent(e.getKey(), e.getValue()));
            add(new Hr());
        });

    }

    private Component chatGroupComponent(LocalDate date, List<ChatHistoryEntity> chats) {
        var layout = new VerticalLayout();
        var label = new Text(date.format(DATE_FORMATTER));
        layout.add(label);
        chats.forEach(m -> layout.add(chatEntryComponent(m)));
        return layout;
    }

    private Component chatEntryComponent(ChatHistoryEntity entity) {
        var text = left(entity.getMessage(), 25);
        var conversationId = entity.getConversationId();

        var layout = new HorizontalLayout();
        layout.setWidthFull();

        var button = new Button(text, e -> {
            getUI().ifPresent(ui -> ui.navigate(ChatView.class, conversationId));
        });
        button.setIcon(new Icon(VaadinIcon.CHAT));
        button.setWidthFull();
        button.getStyle().setTextAlign(Style.TextAlign.LEFT);
        button.addThemeVariants(ButtonVariant.MATERIAL_OUTLINED);

        layout.add(button);

        if (Objects.equals(selectedConversationId, conversationId)) {
            button.setEnabled(false);
        }

        return layout;
    }

    private Component newChatButton() {
        var button = new Button("New Chat", e -> getUI().ifPresent(ui -> ui.navigate(ChatView.class)));
        button.setIcon(new Icon(VaadinIcon.CHAT));
        return button;
    }
}
