package com.qiuye.yeaiagent.tools;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;

/**
 * 网页抓取工具类
 */

public class WebScrapingTool {

    @Tool(description = "This is a tool class used for web page scraping")
    public String scrapWebPage(@ToolParam(description = "This is the link to the webpage") String url){
        try {
            Document document = Jsoup.connect(url).get();
            return document.text();
        } catch (Exception e) {
            return "Failed to scrape the webpage: " + e.getMessage();
        }

    }
}
