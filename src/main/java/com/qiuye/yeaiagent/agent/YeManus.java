package com.qiuye.yeaiagent.agent;

import com.qiuye.yeaiagent.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Component;


@Component
public class YeManus extends ToolCallAgent {

    public YeManus(ToolCallback[] allTools, ChatModel dashscopeChatModel, ToolCallbackProvider toolCallbackProvider) {
        super(allTools);
        this.setName("yeManus");
        String SYSTEM_PROMPT = """  
                You are YeManus, an all-capable AI assistant, aimed at solving any task presented by the user.  
                You have various tools at your disposal that you can call upon to efficiently complete complex requests.  
                """;

        this.setSystemPrompt(SYSTEM_PROMPT);
        String NEXT_STEP_PROMPT = """  
                Based on user needs, proactively select the most appropriate tool or combination of tools.  
                For complex tasks, you can break down the problem and use different tools step by step to solve it.  
                After using each tool, clearly explain the execution results and suggest the next steps.  
                If you want to stop the interaction at any point, use the `terminate` tool/function call.  
                """;

        this.setNextStepPrompt(NEXT_STEP_PROMPT);
        this.setMaxSteps(20);
        // 初始化客户端
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .defaultToolCallbacks(toolCallbackProvider.getToolCallbacks())
                .build();
        this.setChatClient(chatClient);
    }

}
