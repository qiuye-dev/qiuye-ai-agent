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

    @Bean
    public VectorStore loveAppVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel).build();
        //加载文档
        List<Document> documents = loveAppDocumentLoader.loadDocuments();
        //文档增强
        List<Document> enricherDocuments = myDocumentEnricher.enricherDocumentByKeyWord(documents);
//        List<Document> enricherDocuments = myDocumentEnricher.enricherDocumentBySummary(documents);
        simpleVectorStore.add(enricherDocuments);
        return simpleVectorStore;
    }
}
