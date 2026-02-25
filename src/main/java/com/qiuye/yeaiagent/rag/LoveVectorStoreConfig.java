package com.qiuye.yeaiagent.rag;


import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.List;

@Configuration
public class LoveVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Resource
    private MyDocumentEnricher myDocumentEnricher;

    @Resource
    private VectorStore vectorStore;

    @Bean
    public VectorStore loveAppVectorStore() {
        //加载文档
        List<Document> documents = loveAppDocumentLoader.loadDocuments();
        //文档增强
        List<Document> enricherDocuments = myDocumentEnricher.enricherDocumentByKeyWord(documents);
        for (int i = 0; i < enricherDocuments.size(); i += 10) {
            // 为当前批次创建子列表
            List<Document> batch = enricherDocuments.subList(i, Math.min(enricherDocuments.size(), i + 10));
            // 将批次添加到向量存储
            vectorStore.add(batch);
        }
//        List<Document> enricherDocuments = myDocumentEnricher.enricherDocumentBySummary(documents);
        return vectorStore;
    }
}
