package org.eshishkin.demo.ai.chat.front.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLayout;


public class ApplicationView extends VerticalLayout implements RouterLayout {

    public ApplicationView() {
        setHeightFull();
        add(createHeaderContent());
    }

    private Component createHeaderContent() {
        var layout = new HorizontalLayout();

        layout.setId("header");
        layout.getThemeList().set("dark", true);
        layout.setWidthFull();
        layout.setSpacing(false);
        layout.setAlignItems(FlexComponent.Alignment.START);
        layout.add(new H1("Hello Spring AI"));

        return layout;
    }
}
