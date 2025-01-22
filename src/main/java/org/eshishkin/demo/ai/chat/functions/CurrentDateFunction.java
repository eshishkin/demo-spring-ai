package org.eshishkin.demo.ai.chat.functions;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.function.Function;

@Component("CurrentDate")
@Description("Return the current date")
public class CurrentDateFunction implements Function<Void, LocalDate> {

    @Override
    public LocalDate apply(Void unused) {
        return LocalDate.now();
    }
}
