package com.tcpip147.querybook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.parsers.ParserConfigurationException;

@Configuration
@ConditionalOnClass(QueryBook.class)
@EnableConfigurationProperties(QueryBookProperties.class)
public class QueryBookAutoConfiguration {

    @Autowired
    private QueryBookProperties queryBookProperties;

    @Bean
    @ConditionalOnMissingBean
    public QueryBookConfig queryBookConfig() {
        QueryBookConfig queryBookConfig = new QueryBookConfig();
        queryBookConfig.put(QueryBookConfigParams.ROOT, queryBookProperties.getRoot());
        queryBookConfig.put(QueryBookConfigParams.DEV_MODE, queryBookProperties.isDevMode());
        queryBookConfig.put(QueryBookConfigParams.INJECT_COMMENT, queryBookProperties.isInjectComment());
        return queryBookConfig;
    }

    @Bean
    @ConditionalOnMissingBean
    public QueryBook queryBook(QueryBookConfig queryBookConfig) throws ParserConfigurationException {
        return new QueryBook(queryBookConfig);
    }
}
