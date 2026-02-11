package com.qiuye.yeaiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.qiuye.yeaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 文件操作工具类
 * 1. 文件读写：提供读取和写入文件的方法，支持文本文件
 */

public class FileOperationTool {
    private final String FILE_DRI = FileConstant.FILE_SAVE_DIR + "/files/";

    @Tool(description = "the file to be read")
    public String readFile(@ToolParam(description = "The name of the file to be read") String fileName) {
        String filePath = FILE_DRI + fileName;
        try {
            return FileUtil.readUtf8String(filePath);
        } catch (Exception e) {
            return "Error reading file: " + e.getMessage();
        }
    }

    public String writeFile(@ToolParam(description = "The name of the file to be written") String fileName,
                            @ToolParam(description = "The content to be written into the file") String  content
    ) {
        String filePath = FILE_DRI + fileName;
        try {
            FileUtil.writeUtf8String(content, filePath);
            return "File written successfully: " + filePath;
        } catch (Exception e) {
            return "Error writing file: " + e.getMessage();
        }

    }
}
