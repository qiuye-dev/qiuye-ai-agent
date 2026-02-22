package com.qiuye.yeaiagent.agent;

import cn.hutool.core.util.StrUtil;
import com.qiuye.yeaiagent.agent.model.AgentStatus;
import com.qiuye.yeaiagent.exception.BusinessException;
import com.qiuye.yeaiagent.exception.ErrorCode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.ArrayList;
import java.util.List;

/**
 * 抽象基础代理类,用于管理代理状态和执行循环
 */

@Data
@Slf4j
public abstract class BaseAgent {

    //核心属性
    private String name;

    //提示
    private String systemPrompt;
    private String nestStepPrompt;

    //状态
    private AgentStatus status = AgentStatus.IDLE;

    //执行控制
    private int maxSteps = 10;
    private int currentSteps = 0;

    //LLM
    private ChatClient chatClient;

    //memory
    private List<Message> messageList = new ArrayList<>();

    public String run(String userPrompt){
        //基础校验
        if(StrUtil.isBlank(userPrompt)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"Cannot run agent with empty user prompt");
        }
        if(this.status != AgentStatus.IDLE){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"Cannot run agent from status: " + this.status);
        }
        //修改消息
        this.status = AgentStatus.RUNNING;
        //记录消息到上下文
        messageList.add(new UserMessage(userPrompt));
        //保存结果列表
        List<String> results = new ArrayList<>();
        //执行循环
        try {
            for (int i = 0; i < maxSteps && status != AgentStatus.FINISHED; i++) {
                int stepNumber = i + 1;
                currentSteps = stepNumber;
                log.info("Executing step"+ stepNumber + "/" + maxSteps);
                String stepResult = step();
                String result = "Step " + stepNumber + ": " + stepResult;
                results.add(result);
            }
            //检查是否超出步骤
            if(currentSteps >= maxSteps){
                status = AgentStatus.FINISHED;
                results.add("Terminated:Reached maxSteps " + "(" + maxSteps + ")");
            }
            return String.join("\n", results);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,e.getMessage());
        } finally {
            this.clear();
        }

    }

    /**
     * 单个步骤执行
     * @return
     */
    public abstract String step();


    /**
     * 清理资源
     */
    protected void clear(){
        messageList.clear();
    }

}
