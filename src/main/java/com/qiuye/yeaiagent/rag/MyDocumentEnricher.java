package com.qiuye.yeaiagent.rag;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.model.transformer.SummaryMetadataEnricher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *文档增强器
 */

@Component
public class MyDocumentEnricher {

    private final ChatModel chatModel;

    MyDocumentEnricher(ChatModel dashscopeChatModel) {
        this.chatModel = dashscopeChatModel;
    }
    /**
     * 通过关键词增强文档
     * @param documents
     * @return
     */
    public List<Document> enricherDocumentByKeyWord(List<Document> documents){
        return new KeywordMetadataEnricher(this.chatModel, 5)
                .apply(documents);
    }

    /**
     * 摘要文档增强器
     * @param documents
     * @return
     */
    public List<Document> enricherDocumentBySummary(List<Document> documents){
        return new SummaryMetadataEnricher(this.chatModel,
                List.of(SummaryMetadataEnricher.SummaryType.PREVIOUS, SummaryMetadataEnricher.SummaryType.CURRENT, SummaryMetadataEnricher.SummaryType.NEXT))
                .apply(documents);
    }
}
