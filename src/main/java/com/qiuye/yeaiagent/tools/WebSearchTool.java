package com.qiuye.yeaiagent.tools;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 联网搜索工具类
 */

public class WebSearchTool {

    private static final String SEARCH_API_URL = "https://www.searchapi.io/api/v1/search";

    @Value("${search-api.api-key}")
    private final String apiKey;

    public WebSearchTool(String apiKey) {
        this.apiKey = apiKey;
    }

    @Tool(description = "This is a tool class for querying web pages")
    public String searchWeb(@ToolParam(description = "This is to query the related words of the webpage") String query) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("q", query);
        params.put("api_key", apiKey);
        params.put("engine", "baidu");

        try {
            String response = HttpUtil.get(SEARCH_API_URL, params);

            JSONObject jsonObject = JSONUtil.parseObj(response);
            //获取搜索结果中的organic_results字段
            JSONArray jsonArray = jsonObject.getJSONArray("organic_results");
            //删除不需要的信息
            jsonArray.remove("position");
            jsonArray.remove("snippet");
            jsonArray.remove("snippet_highlighted_words");
            jsonArray.remove("thumbnail");
            //取前5条
            List<Object> objects = jsonArray.subList(0, 5);
            //拼接字符串
            return objects.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(","));
        } catch (Exception e) {
            return "Search failed. Error message：" + e.getMessage();
        }
    }
}
