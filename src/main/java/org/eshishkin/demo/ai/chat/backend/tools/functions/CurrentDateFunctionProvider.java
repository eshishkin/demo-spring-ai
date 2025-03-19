package org.eshishkin.demo.ai.chat.backend.tools.functions;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class CurrentDateFunctionProvider {

    @Tool(name = "CurrentDate", description = "Return the current date")
    public LocalDate getCurrentDate() {
        return LocalDate.now();
    }
}
