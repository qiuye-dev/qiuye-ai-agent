package com.qiuye.yeaiagent.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;
import io.netty.resolver.ResolvedAddressTypes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ReactorResourceFactory;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
public class NettyClientConfig {
    
    @Bean
    public ReactorResourceFactory reactorResourceFactory() {
        ReactorResourceFactory factory = new ReactorResourceFactory();

        factory.setUseGlobalResources(false);

        // 自定义ConnectionProvider，配置DNS解析
        ConnectionProvider connectionProvider = ConnectionProvider.builder("dashscope-pool")
            .maxConnections(500)
            .maxIdleTime(Duration.ofSeconds(20))
            .maxLifeTime(Duration.ofMinutes(5))
            .pendingAcquireTimeout(Duration.ofSeconds(60))
            .evictInBackground(Duration.ofSeconds(120))
            .build();
        factory.setConnectionProvider(connectionProvider);
        return factory;
    }

    /**
     * 使用自定义的DNS解析器
     * @param reactorResourceFactory
     * @return
     */
    @Bean
    public HttpClient httpClient(ReactorResourceFactory reactorResourceFactory) {
        return HttpClient.create(reactorResourceFactory.getConnectionProvider())
                .baseUrl("https://dashscope.aliyuncs.com")
                .resolver(spec -> {
                            // 配置 DNS 解析器，使用 Google DNS 并强制使用 IPv4
                            spec.queryTimeout(Duration.ofSeconds(10))
                                    .cacheMinTimeToLive(Duration.ofSeconds(60))
                                    .cacheMaxTimeToLive(Duration.ofSeconds(300))
                                    .resolvedAddressTypes(ResolvedAddressTypes.IPV4_ONLY)// 强制只使用 IPv4
                                    .trace("DNS", LogLevel.DEBUG)
                                    .build();
                })
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .responseTimeout(Duration.ofSeconds(30));
    }
}