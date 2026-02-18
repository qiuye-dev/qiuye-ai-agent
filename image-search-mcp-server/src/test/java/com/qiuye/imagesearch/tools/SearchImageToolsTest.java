package com.qiuye.imagesearch.tools;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SearchImageToolsTest {

    @Resource
    private SearchImageTools searchImageTools;

    @Test
    void searchImageTool() {
        String result = searchImageTools.searchImageTool("computer");
        System.out.println(result);
        Assertions.assertNotNull(result);
    }
}



