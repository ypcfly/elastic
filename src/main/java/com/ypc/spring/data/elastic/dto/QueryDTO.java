package com.ypc.spring.data.elastic.dto;


import lombok.Data;

@Data
public class QueryDTO {

    private Boolean condition;

    private int pageNum;

    private int pageSize;

    private String userCode;

    private Integer minAge;

    private Integer maxAge;
}
