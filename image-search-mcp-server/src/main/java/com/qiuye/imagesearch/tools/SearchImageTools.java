package com.qiuye.imagesearch.tools;


import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.util.HashMap;
import java.util.stream.Collectors;

@Component
public class SearchImageTools {

    @Value("${pexels-api.apikey}")
    private String apikey;

    private final String searchImageUrl = "https://api.pexels.com/v1/search";

    @Tool(description = "Search for pictures based on keywords")
    public String searchImageTool(@ToolParam(description = "Search keywords for images") String query){
        try {
            //拼接参数
            HashMap<String,String> headers = new HashMap<>();
            headers.put("Authorization",apikey);
            HashMap<String,Object> params = new HashMap<>();
            params.put("query",query);
            //请求地址
            String response = HttpUtil.createGet(searchImageUrl)
                    .addHeaders(headers)
                    .form(params)
                    .execute()
                    .body();

            return JSONUtil.parseObj(response)
                    .getJSONArray("photos")
                    .stream()
                    .map(objPhoto -> (JSONObject) objPhoto)
                    .map(objPhoto -> objPhoto.getJSONObject("src"))
                    .map(objSrc -> objSrc.getStr("medium"))
                    .filter(str -> StrUtil.isNotBlank(str))
                    .collect(Collectors.joining(","));
        } catch (HttpException e) {
            return "Error image: " + e.getMessage();
        }
    }

}
