package com.qiuye.yeaiagent.app;


import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.qiuye.yeaiagent.advisor.MyLoggerAdvisor;
import com.qiuye.yeaiagent.advisor.ReReadingAdvisor;
import com.qiuye.yeaiagent.chatmemory.FileBaseChatMemory;
import com.qiuye.yeaiagent.rag.LoveAppRagCustomAdvisorFactory;
import com.qiuye.yeaiagent.rag.QueryRewrite;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
@Slf4j
public class LoveApp{


    private final ChatClient chatClient;


    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

    record LoveReport(String titel, List<String> suggestion){}


    public LoveApp(ChatModel dashscopeChatModel) {
        //初始化基于文件的记忆对话
        String filePath = System.getProperty("user.dir") + "/tmp/"+ "/chat-memories/";
        ChatMemory chatMemory = new FileBaseChatMemory(filePath);
//        //初始化基于内存的记忆对话
//        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
//                .chatMemoryRepository(new InMemoryChatMemoryRepository())
//                .maxMessages(10)
//                .build();

        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new MyLoggerAdvisor(),
                        new ReReadingAdvisor()
//                        new CheckBannedAdvisor()
                )
                .build();
    }

    public String doChat(String message, String chatId){

        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        return chatResponse.getResult().getOutput().getText();
    }


    public LoveReport doChatReport(String message, String chatId){
        LoveReport chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .entity(LoveReport.class);

        return chatResponse;
    }


    public Flux<String> doChatWithStream(String message, String chatId){
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }




    @Resource
    private VectorStore loveAppVectorStore;

    @Resource
    private QueryRewrite QueryRewrite;

    @Resource
    ToolCallback[] allTools;


    public String doChatWithRag(String message, String chatId){
        //对用户的查询进行改写，改写后更适合检索相关知识
        String queryRewritePrompt = QueryRewrite.doQueryWrite(message);
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(queryRewritePrompt)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(QuestionAnswerAdvisor.builder(loveAppVectorStore).searchRequest(SearchRequest.builder().build()).build())
                .advisors(LoveAppRagCustomAdvisorFactory.createDocumentRetrieverAdvisor(loveAppVectorStore, "已婚"))
                .toolCallbacks(allTools)
                .call()
                .chatResponse();
        return chatResponse.getResult().getOutput().getText();
    }

    @Resource
    private ToolCallbackProvider toolCallbackProvider;




}


