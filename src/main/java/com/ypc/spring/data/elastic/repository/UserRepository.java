package com.ypc.spring.data.elastic.repository;

import com.ypc.spring.data.elastic.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface UserRepository extends ElasticsearchRepository<UserEntity,String> {

    @Query("{\"bool\": {\"must\": [{ \"query_string\": { \"default_field\": \"userCode\",\"query\": \"*?0*\"}},{ \"range\": {\"userAge\": {\"gte\": ?1,\"lte\": ?2}}}]}}")
    Page<UserEntity> queryPage(String userCode,Integer min, Integer max, PageRequest pageRequest);
}
