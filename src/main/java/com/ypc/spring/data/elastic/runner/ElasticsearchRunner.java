package com.ypc.spring.data.elastic.runner;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.ypc.spring.data.elastic.entity.OrderEntity;
import com.ypc.spring.data.elastic.entity.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ElasticsearchRunner implements ApplicationRunner {

    @Autowired
    private RestHighLevelClient restHighLevelClient;
    private static final String USER_INDEX_NAME = "user_entity";
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Override
    public void run(ApplicationArguments args) throws Exception {
        GetIndexRequest getIndexRequest = new GetIndexRequest(USER_INDEX_NAME);
        Boolean exist = restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        // 不存在则创建index和setting mapping
        if (!exist) {
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(USER_INDEX_NAME);
            Settings settings = Settings.builder()
                    .put("index.number_of_shards",1)
                    .put("index.number_of_replicas",1)
                    .build();
            Map<String,Object> propertyMap = createIndexMapping();
            createIndexRequest.settings(settings).mapping(propertyMap);

            CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(createIndexRequest,RequestOptions.DEFAULT);
            if (!createIndexResponse.isAcknowledged()) {
                log.error(">>>> 创建索引和映射关系失败! <<<<");
                throw new RuntimeException("创建索引和映射关系失败");
            }
        }
    }

    private Map<String,Object> createIndexMapping() {
        Map<String,Object> resultMap = new HashMap<>();
        Map<String,Object> fieldsMap = new HashMap<>();
        UserEntity userEntity = new UserEntity();
        Map<String,Object> beanMap = BeanUtil.beanToMap(userEntity,false,false);
        for (Map.Entry<String,Object> entry : beanMap.entrySet()) {
            String key = entry.getKey();
            Map<String,Object> map = new HashMap<>();
            if ("id".equals(key)) {
                Map<String,Object> map2 = new HashMap<>();
                map2.put("type","keyword");
                map2.put("ignore_above",256);
                Map<String,Object> map1 = new HashMap<>();
                map1.put("keyword",map2);
                map.put("type","text");
                map.put("fields",map1);
            } else if ("orderEntityList".equals(key)) {
                map = createNested();
            } else {
                map.put("type","keyword");
                map.put("store",true);
            }
            fieldsMap.put(key,map);
        }
        resultMap.put("properties",fieldsMap);
        return resultMap;
    }

    private Map<String, Object> createNested() {
        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("type","nested");
        Map<String,Object> nestedMap = generateMap();
        resultMap.put("properties",nestedMap);
        return resultMap;
    }

    private Map<String, Object> generateMap() {
        OrderEntity orderEntity = new OrderEntity();
        Map<String,Object> map = BeanUtil.beanToMap(orderEntity,false,false);
        Map<String,Object> resultMap = new HashMap<>();
        for (Map.Entry<String,Object> entry: map.entrySet()) {
            String key = entry.getKey();
            Map<String,Object> field = new HashMap<>();
            if ("updateTime".equals(key) || "createTime".equals(key)) {
                field.put("type","date");
                field.put("store",true);
                Date date = (Date) entry.getValue();
                field.put("format", DateUtil.format(date,DATE_FORMAT));
            } else {
                field.put("type","keyword");
                field.put("store",true);
            }
            resultMap.put(key,field);
        }
        return resultMap;
    }
}
