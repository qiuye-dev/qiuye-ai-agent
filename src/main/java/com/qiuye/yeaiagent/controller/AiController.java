package com.qiuye.yeaiagent.controller;


import com.qiuye.yeaiagent.agent.YeManus;
import com.qiuye.yeaiagent.app.LoveApp;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RequestMapping("ai")
@RestController
@Slf4j
public class AiController {

    @Resource
    private LoveApp loveApp;

    @Resource
    private YeManus yeManus;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;


    @GetMapping("/love_app/chat/sync")
    public String doChatWithLoveAppSync(String message, String chatId) {
        return loveApp.doChat(message, chatId);
    }

    @GetMapping(value = "/love_app/chat/ss")
    public Flux<ServerSentEvent<String>> doChatWithLoveAppServerSentEvent(String message, String chatId) {
        return loveApp.doChatWithStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    /**
     * 采用sse流式输出
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping("love_app/chat/sseEmitter")
    public SseEmitter doChatWithSse(String message, String chatId){
        SseEmitter sseEmitter = new SseEmitter(180000L);
        loveApp.doChatWithStream(message,chatId)
                .subscribe(chunk -> {
                    try {
                        sseEmitter.send(SseEmitter.event()
                                .data(chunk)
                                .id(String.valueOf(System.currentTimeMillis())));  // 包装成标准 SSE 格式
                    } catch (Exception e) {
                        sseEmitter.completeWithError(e);
                    }
                },
                        // 处理错误：先尝试发一条可读的错误事件，再结束连接
                        err -> {
                            try {
                                String errorMsg = "流式处理出错: " + (err == null ? "未知错误" : err.getMessage());
                                sseEmitter.send(SseEmitter.event()
                                        .name("error")
                                        .data(errorMsg)
                                        .id(String.valueOf(System.currentTimeMillis())));
                            } catch (Exception sendEx) {
                                // ignore send failures
                            } finally {
                                sseEmitter.completeWithError(err);
                            }
                        },
                         //处理完成
                         sseEmitter::complete);
        return sseEmitter;
    }



    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSSE(String message, String chatId) {
        return loveApp.doChatWithStream(message, chatId);
    }

    @GetMapping(value = "/love_app/chat/agent")
    public String doChatWithAgent(String message){
        return yeManus.run(message);
    }

    @GetMapping(value = "/love_app/chat/agent/sse")
    public SseEmitter doChatWithAgentSse(String message) throws Exception {
        return yeManus.runStream(message);
    }

}