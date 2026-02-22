package com.qiuye.yeaiagent.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class YeManusTest {

    @Resource
    private YeManus yeManus;

    @Test
    void run() {
        String userPrompt = """  
                我的另一半居住在上海静安区，请帮我找到 5 公里内合适的约会地点,
                并结合一些网络图片，制定一份详细的约会计划，
                并以 PDF 格式和中文形式输出""";
        String answer = yeManus.run(userPrompt);
        Assertions.assertNotNull(answer);
    }
}
