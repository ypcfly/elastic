package com.ypc.spring.data.elastic.runner;

import com.ypc.spring.data.elastic.entity.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserRunner implements ApplicationRunner {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Value("${spring.es.user.index}")
    private String indexName;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        IndexCoordinates indexCoordinates = IndexCoordinates.of(indexName);
        IndexOperations indexOperations = elasticsearchOperations.indexOps(indexCoordinates);
        if (!indexOperations.exists()) {
            // 创建索引
            indexOperations.create();
            indexOperations.refresh();
            // 将映射关系写入到索引，即将数据结构和类型写入到索引
            indexOperations.putMapping(UserEntity.class);
            indexOperations.refresh();
            log.info(">>>> 创建索引和映射关系成功 <<<<");
        }
    }
}
