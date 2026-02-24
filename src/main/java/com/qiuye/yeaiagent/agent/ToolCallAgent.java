package com.qiuye.yeaiagent.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.qiuye.yeaiagent.agent.model.AgentStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 *
 */

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent{
     //可用工具列表
    private final ToolCallback[] availableTools;

    //保存工具响应
    private ChatResponse toolCallChatResponse;

    //工具管理者
    private ToolCallingManager toolCallingManager;

    //禁用内置的工具调用机制,自己维护
    private ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools){
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        this.chatOptions = DashScopeChatOptions.builder()
                .internalToolExecutionEnabled(false)
                .build();
    }

    @Override
    public boolean think() {
        if(getNextStepPrompt() != null && !StrUtil.isEmpty(getNextStepPrompt())){
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
        }
        List<Message> messageList = getMessageList();
        Prompt prompt = new Prompt(messageList, chatOptions);
        try {
            //获取带工具选项的响应
            ChatResponse chatResponse = getChatClient()
                    .prompt(prompt)
                    .system(getSystemPrompt())
                    .toolCallbacks(availableTools)
                    .call()
                    .chatResponse();
            //记录响应,用于Act
            toolCallChatResponse = chatResponse;
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            //响应结果
            String result = assistantMessage.getText();
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            log.info(getName() + "思考了: " + result);
            log.info(getName() + "选择了: " + toolCallList.size() + "个工具来使用");
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> {
                        return String.format("工具名称: %s, 参数: %s", toolCall.name(), toolCall.arguments());
                    }).collect(Collectors.joining("\n"));
            log.info(toolCallInfo);
            //检查agent是否调用了AskHuman工具,并获取问题参数
            String question = toolCallList.stream()
                    .filter(toolCall -> "askHuman".equals(toolCall.name()))
                    .map(AssistantMessage.ToolCall::arguments)
                    .findFirst()
                    .filter(ObjUtil::isNotNull)
                    .orElse(null);
            if(StrUtil.isNotEmpty(question)){
                //向用户输出问题
                System.out.println("智能体需要你的帮助,问题: " + question);
                //提取用户输入
                Scanner scanner = new Scanner(System.in);
                String userAnswer = scanner.nextLine();
                //将用户输入添加到消息列表
                UserMessage userMessage = new UserMessage("用户回答" + userAnswer);
                getMessageList().add(userMessage);
                return true;
            }
            //如果没有调用工具添加助手消息到消息上下文
            if(toolCallList.isEmpty()){
                getMessageList().add(assistantMessage);
                return false;
            }else {
                //需要调用工具时不用添加,因为调用工具时会自动添加
                return true;
            }
        } catch (Exception e) {
            log.error(getName() + "思考的过程中遇到了问题: " + e.getMessage());
            getMessageList().add(
                    new AssistantMessage("处理室遇到问题: " + e.getMessage())
            );
            return false;
        }
    }

    @Override
    public String act() {
        if(!toolCallChatResponse.hasToolCalls()){
            return "没有工具调用";
        }
        //调用工具
        Prompt prompt = new Prompt(getMessageList(), chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        //记录上下文, conversationHistory中包含了工具执行结果和助手消息
        setMessageList(toolExecutionResult.conversationHistory());
        //工具调用结束后
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        String result = toolResponseMessage.getResponses().stream()
                .map(toolResponse -> "工具: " + toolResponse.name() + "完成了调用,结果: " + toolResponse.responseData())
                .collect(Collectors.joining("\n"));
        //判断是否调用了终止工具
        boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                .anyMatch(toolResponse -> "doTerminate".equals(toolResponse.name()));
        if(terminateToolCalled){
            setStatus(AgentStatus.FINISHED);
        }
        log.error(result);
        return result;
    }
}
