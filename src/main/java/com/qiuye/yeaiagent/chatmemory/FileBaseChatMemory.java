package com.qiuye.yeaiagent.chatmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于文件实现的记忆持久化
 */

public class FileBaseChatMemory implements ChatMemory {

    private final String filePath;

    public static final Kryo kryo = new Kryo();

    static {
        kryo.setRegistrationRequired(false);
        //设置实例化策略
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    public FileBaseChatMemory(String filePath) {
        this.filePath = filePath;
        File file = new File(filePath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        List<Message> conversationMessages = new ArrayList<>(getOrCreateConversationMessages(conversationId));
        conversationMessages.addAll(messages);
        saveConversationFile(conversationId, conversationMessages);
    }

    @Override
    public List<Message> get(String conversationId) {
        return getOrCreateConversationMessages(conversationId);
    }

    @Override
    public void clear(String conversationId) {
        clearAll(conversationId);
    }

    public void clearAll(String conversationId) {
        File file = this.getConversationFile(conversationId);
        if(file.exists()){
            file.delete();
        }
    }

    /**
     * 获取或创建对话消息列表
     * @param conversationId
     * @return
     */
    private List<Message> getOrCreateConversationMessages(String conversationId) {
        File file = getConversationFile(conversationId);
        if (!file.exists()) {
            return List.of();
        }
        ArrayList<Message> messages = new ArrayList<>();
        try (Input input = new Input(new FileInputStream(file))) {
            messages =  kryo.readObject(input, ArrayList.class);
        } catch (Exception e) {
            throw new RuntimeException("读取对话文件失败", e);
        }
        return messages;
    }

    /**
     * 保存对话文件
      * @param conversationId
     * @param messages
     */
    private void saveConversationFile(String conversationId, List<Message> messages) {
        File file = getConversationFile(conversationId);
        try (Output output = new Output(new FileOutputStream(file))) {
            kryo.writeObject(output, messages);
        } catch (Exception e) {
            throw new RuntimeException("保存对话文件失败", e);
        }
    }

    /**
     * 获取文件
     * @param conversationId
     * @return
     */
    private File getConversationFile(String conversationId) {
        return new File(filePath, conversationId + ".kryo");
    }
}
