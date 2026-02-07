package com.qiuye.yeaiagent.advisor;


import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.List;

/**
 * 检查是否有违规内容的Advisor
 */

@Slf4j
public class CheckBannedAdvisor implements BaseAdvisor {

    public static final List<String> SENSITIVE_WORDS = new ArrayList<>(List.of(
            "暴力",
            "色情",
            "赌博",
            "毒品",
            "恐怖主义",
            "极端主义",
            "仇恨言论"
    ));

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        return BaseAdvisor.super.adviseCall(chatClientRequest, callAdvisorChain);
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        return BaseAdvisor.super.adviseStream(chatClientRequest, streamAdvisorChain);
    }



    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        boolean result = sensitiveCheck(chatClientRequest);
        if(result){
            log.warn("请求中包含敏感词汇，已被拦截: {}", chatClientRequest.prompt().getUserMessage().getText());
            throw new RuntimeException("请求中包含敏感词汇，已被拦截");
        }
        return chatClientRequest;
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return null;
    }


    /**
     * 敏感词汇检测
     * @param chatClientRequest
     * @return
     */
    public boolean sensitiveCheck(ChatClientRequest chatClientRequest) {
        String userText = chatClientRequest.prompt().getUserMessage().getText();
        for (String word : SENSITIVE_WORDS) {
            if (userText.contains(word)) {
                log.warn("检测到敏感词汇: {}", word);
                return true;
            }
        }
        return false;
    }



    @Override
    public String getName() {
        return BaseAdvisor.super.getName();
    }

    @Override
    public int getOrder() {
        return -10;
    }
}
