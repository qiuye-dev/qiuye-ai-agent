package com.qiuye.yeaiagent.rag;

import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

/**
 * 自定义 RAG 查询增强 Advisor工厂类
 */


public class LoveAppRagCustomAdvisorFactory {



    public static Advisor createDocumentRetrieverAdvisor(VectorStore LoveAppVectorStore, String status) {
        Filter.Expression expression = new FilterExpressionBuilder().eq("status", status).build();

        VectorStoreDocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(LoveAppVectorStore)
                .topK(5)
                .similarityThreshold(0.75)
                .filterExpression(expression)
                .build();

        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(retriever)
                .queryAugmenter(LoveAppContextualQueryAugmenterFactory.createInstance())
                .build();

    }
}
