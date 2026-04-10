package com.qiuye.yeaiagent.controller;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
class AiControllerTest {

    @Resource
    private AiController aiController;

    @Test
    void doChatWithSse() {
        aiController.doChatWithSse("我是秋叶","1");
    }

//    @Test
//    void doChatWithLoveAppSSE() {
//        aiController.doChatWithLoveAppSSE("我是秋叶","1");
//    }
}