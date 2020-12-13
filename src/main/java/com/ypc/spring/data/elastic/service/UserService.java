package com.ypc.spring.data.elastic.service;

import com.ypc.spring.data.elastic.dto.QueryDTO;
import com.ypc.spring.data.elastic.entity.UserEntity;
import org.springframework.data.domain.Page;

public interface UserService {
    Page<UserEntity> pageQuery(QueryDTO queryDTO);


    UserEntity save(UserEntity userEntity);

    UserEntity queryById(String id);

    void deleteById(String id);

    UserEntity update(UserEntity userEntity);
}
