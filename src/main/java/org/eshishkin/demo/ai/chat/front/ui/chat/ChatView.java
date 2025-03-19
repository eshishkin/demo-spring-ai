package org.eshishkin.demo.ai.chat.front.ui.chat;

import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.eshishkin.demo.ai.chat.backend.service.ChatService;
import org.eshishkin.demo.ai.chat.front.ui.ApplicationView;

import java.util.UUID;

@PageTitle("Hello Spring AI")
@Route(value = "", layout = ApplicationView.class)
public class ChatView extends SplitLayout implements HasUrlParameter<String> {
    private final ChatService chat;

    public ChatView(ChatService chat) {
        this.chat = chat;

        setId("chat-page");
        setWidthFull();
        setHeightFull();
        setOrientation(Orientation.HORIZONTAL);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        var selectedConversationId = parameter != null ? parameter : UUID.randomUUID().toString();

        setSplitterPosition(20);
        addToPrimary(new ChatHistoryPanel(selectedConversationId, chat));
        addToSecondary(new ChatPanel(selectedConversationId, chat));
    }
}
