package com.ypc.spring.data.elastic.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
//@Document(indexName = "user_entity_index",shards = 1,replicas = 1,createIndex = true)
public class UserEntity {

    @Id
    private String id;

    @Field(type = FieldType.Keyword, store = true)
    private String userName;

    @Field(type = FieldType.Keyword, store = true)
    private String userCode;

    @Field(type = FieldType.Keyword, store = true)
    private String userAddress;

    @Field(type = FieldType.Keyword, store = true)
    private String userMobile;

    @Field(type = FieldType.Integer, store = true)
    private Integer userGrade;

    @Field(type = FieldType.Nested, store = true)
    private List<OrderEntity> orderEntityList;

    @Field(type = FieldType.Keyword, store = true)
    private String status;

    @Field(type = FieldType.Integer, store = true)
    private Integer userAge;
}
