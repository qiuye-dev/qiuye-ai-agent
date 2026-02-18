package com.qiuye.yeaiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
class MyMultiQueryExpanderTest {

    @Resource
    private MyMultiQueryExpander myMultiQueryExpander;

    @Test
    void multiQueryExpander() {
        myMultiQueryExpander.multiQueryExpander();
    }
}