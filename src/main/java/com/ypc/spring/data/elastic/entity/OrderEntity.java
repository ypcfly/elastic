package com.ypc.spring.data.elastic.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

@Data
public class OrderEntity {

    @Field(type = FieldType.Keyword, store = true)
    private String id;

    @Field(type = FieldType.Keyword, store = true,index = false)
    private String orderNum;

    @Field(type = FieldType.Date,format = DateFormat.custom,pattern = "yyyy-MM-dd HH:mm:ss",store = true)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;

    @Field(type = FieldType.Date,format = DateFormat.custom,pattern = "yyyy-MM-dd HH:mm:ss",store = true)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updateTime;

    @Field(type = FieldType.Keyword, store = true)
    private String amount;

    @Field(type = FieldType.Keyword, store = true)
    private String userId;

    @Field(type = FieldType.Keyword, store = true)
    private String mobile;

    @Field(type = FieldType.Keyword, store = true)
    private String status;

}
