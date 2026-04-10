package com.qiuye.yeaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

public class AskHuman {

    @Tool(description = "Use this tool to ask human for help")
    private String askHuman(@ToolParam(description = "The question you want to ask human") String question){
        return question;
    }
}
