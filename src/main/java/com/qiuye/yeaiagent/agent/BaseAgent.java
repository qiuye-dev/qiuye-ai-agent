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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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
    private String nextStepPrompt;

    //状态
    private AgentStatus status = AgentStatus.IDLE;

    //执行控制
    private int maxSteps = 10;
    private int currentSteps = 0;

    //LLM
    private ChatClient chatClient;

    //memory
    private List<Message> messageList = new ArrayList<>();

    //重复阈值
    private int duplicateThreshold = 2;

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
                if(isStuck()){
                    handleStuckState();
                }
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

    public SseEmitter runStream(String userPrompt) throws Exception {
        SseEmitter sseEmitter = new SseEmitter(300000L);

        CompletableFuture.runAsync(() -> {
            //基础校验
            try {
                if (StrUtil.isBlank(userPrompt)) {
                    sseEmitter.send("Cannot run agent with empty user prompt");
                    sseEmitter.complete();
                    return;
                }
                if (this.status != AgentStatus.IDLE) {
                    sseEmitter.send("Cannot run agent from status: " + this.status);
                    sseEmitter.complete();
                    return;
                }
                //执行循环
                try {
                    for (int i = 0; i < maxSteps && status != AgentStatus.FINISHED; i++) {
                        int stepNumber = i + 1;
                        currentSteps = stepNumber;
                        log.info("Executing step"+ stepNumber + "/" + maxSteps);
                        String stepResult = step();
                        if(isStuck()){
                            handleStuckState();
                        }
                        String result = "Step " + stepNumber + ": " + stepResult;
                        sseEmitter.send(result);
                    }
                    //检查是否超出步骤
                    if(currentSteps >= maxSteps){
                        status = AgentStatus.FINISHED;
                        sseEmitter.send("Terminated:Reached maxSteps " + "(" + maxSteps + ")");
                    }

                } catch (Exception e) {
                    log.error("Error occurred while running agent", e);
                    this.status = AgentStatus.ERROR;
                    try {
                        sseEmitter.send("Error occurred while running agent: " + e.getMessage());
                        sseEmitter.complete();
                    } catch (IOException ex) {
                        sseEmitter.completeWithError(ex);
                    }
                } finally {
                    this.clear();
                }
            }catch (Exception e){
                sseEmitter.completeWithError(e);
            }
        });

        //设置超时处理和完成回溯
        sseEmitter.onTimeout(() -> {
            this.status = AgentStatus.ERROR;
            this.clear();
            log.error("Agent execution timed out");
        });

        sseEmitter.onCompletion(() -> {
            if(this.status == AgentStatus.RUNNING){
                this.status = AgentStatus.FINISHED;
            }
            this.clear();
            log.info("Agent execution completed");
        });
        return sseEmitter;
    }


    protected void handleStuckState(){
        String stuckPrompt = "Repeated responses have been observed. " +
                "Consider a new strategy to avoid repeating the ineffective paths that have already been tried";
        nextStepPrompt = stuckPrompt + "\n" + (this.nextStepPrompt != null ? this.nextStepPrompt : "");
        log.info("Agent detected stuck state. Added prompt:" + stuckPrompt);
    }


    /**
     * 检查是否进入了循环
     * @return true表示进入 false表示没进
     */
    protected boolean isStuck(){
        //当前的消息列表
        List<Message> messages = this.messageList;
        if(messages.size() < 2){
            return false;
        }
        Message lastMessage = messages.get(messages.size() - 1);
        if(lastMessage.getText() == null || lastMessage.getText().isEmpty()){
            return false;
        }
        //计算重复出现的消息
        int duplicateCount = 0;
        for(int i = messages.size() - 2; i >= 0; i--){
            Message message = messages.get(i);
            if(message.getText().equals(lastMessage.getText())){
                duplicateCount++;
            }
        }
        return duplicateCount >= duplicateThreshold;
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
