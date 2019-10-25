package com.management.notes.elastic;

import org.elasticsearch.client.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

@Configuration
public class ElasticConfig {


    @Bean
    public ElasticsearchTemplate elasticsearchTemplate(final Client client) {
        return new ElasticsearchTemplate(client, new CustomEntityMapper());
    }
}
