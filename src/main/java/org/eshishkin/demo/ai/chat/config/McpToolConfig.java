package org.eshishkin.demo.ai.chat.config;

import org.eshishkin.demo.ai.chat.functions.CurrentDateFunctionProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolConfig {

    @Bean
    public ToolCallbackProvider tools(CurrentDateFunctionProvider dateFunction) {
        return MethodToolCallbackProvider.builder().toolObjects(dateFunction).build();
    }
}
