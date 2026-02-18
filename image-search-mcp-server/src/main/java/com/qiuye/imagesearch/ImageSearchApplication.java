package com.qiuye.imagesearch;

import com.qiuye.imagesearch.tools.SearchImageTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ImageSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImageSearchApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider ImageSearchTool(SearchImageTools searchImageTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(searchImageTools)
                .build();
    }
}
