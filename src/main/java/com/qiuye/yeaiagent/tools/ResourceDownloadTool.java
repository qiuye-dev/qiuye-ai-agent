package com.qiuye.yeaiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.qiuye.yeaiagent.constant.FileConstant;
import opennlp.tools.util.DownloadUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 下载资源的工具类
 */

public class ResourceDownloadTool {


    @Tool(description = "This is a tool class for downloading resources")
    public String downloadResource(@ToolParam(description = "This is the website link for downloading resources") String url,
                                   @ToolParam(description = "This is the filename where the resources have been saved") String fileName
    ){
        String dir = FileConstant.FILE_SAVE_DIR + "/download";
        String filePath = dir + "/" + fileName;

        //创建目录
        try {
            FileUtil.mkdir(filePath);
            //下载资源
            HttpUtil.downloadFile(url,filePath);
            return "Resource downloaded successfully, saved at: " + filePath;
        } catch (Exception e) {
            return "Resource download failed, " + e.getMessage();
        }

    }
}
