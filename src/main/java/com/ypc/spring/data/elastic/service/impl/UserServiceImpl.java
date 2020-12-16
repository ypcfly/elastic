package com.ypc.spring.data.elastic.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.ypc.spring.data.elastic.dto.QueryDTO;
import com.ypc.spring.data.elastic.entity.OrderEntity;
import com.ypc.spring.data.elastic.entity.UserEntity;
import com.ypc.spring.data.elastic.repository.UserRepository;
import com.ypc.spring.data.elastic.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.elasticsearch.core.query.UpdateResponse;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Page<UserEntity> pageQuery(QueryDTO queryDTO) {
        // 分页默认从0开始，按照userGrade逆向排序
        PageRequest pageRequest = PageRequest.of(queryDTO.getPageNum() - 1,queryDTO.getPageSize(), Sort.by(Sort.Direction.DESC,"userAge"));
        Page<UserEntity> page = null;
        // 条件查询
        if (Boolean.TRUE.equals(queryDTO.getCondition())) {
            Integer min = queryDTO.getMinAge();
            Integer max = queryDTO.getMaxAge();
            String userCode = queryDTO.getUserCode();
            page = userRepository.queryPage(userCode,min,max,pageRequest);
        } else {
            // 查询所有
            page = userRepository.findAll(pageRequest);
        }
        return page;
    }

    @Override
    public UserEntity save(UserEntity userEntity) {
        List<OrderEntity> orderEntityList = new ArrayList<>();
        String userId = IdUtil.simpleUUID();
        // 自定义Id覆盖
        userEntity.setId(userId);
        for (int i = 0; i < 4; i++) {
            OrderEntity orderEntity = new OrderEntity();
            setProperties(orderEntity,i);
            orderEntity.setUserId(userId);
            orderEntityList.add(orderEntity);
        }
        userEntity.setOrderEntityList(orderEntityList);
        return userRepository.save(userEntity);
    }

    private String updateUser(UserEntity userEntity) {
        log.info(">>>> user={} <<<<",userEntity);
        Map<String,Object> paramMap = new HashMap<>();
        
        paramMap.put("id",userEntity.getId());
        paramMap.put("userMobile",userEntity.getUserMobile());
        UpdateQuery updateQuery = UpdateQuery.builder(userEntity.getId()).withDocument(Document.from(paramMap)).build();
        UpdateResponse updateResponse = elasticsearchRestTemplate.update(updateQuery, IndexCoordinates.of("user_entity_index"));
        UpdateResponse.Result result = updateResponse.getResult();
        return String.valueOf(result);
    }

    @Override
    public UserEntity queryById(String id) {
        Optional<UserEntity> optional = userRepository.findById(id);
        return optional.isPresent() ? optional.get() : null;
    }

    @Override
    public void deleteById(String id) {
        userRepository.deleteById(id);
    }

    @Override
    public UserEntity update(UserEntity userEntity) {
//        Optional<UserEntity> optional = userRepository.findById(userEntity.getId());
//        if (!optional.isPresent()) {
//            return null;
//        }
//        List<OrderEntity> orderEntityList = optional.get().getOrderEntityList();
//        userEntity.setOrderEntityList(orderEntityList);
//        return userRepository.save(userEntity);

        String result = updateUser(userEntity);
        Optional<UserEntity> optional = userRepository.findById(userEntity.getId());
        return optional.isPresent() ? optional.get() : null;
    }

    private void setProperties(OrderEntity orderEntity, int i) {
        Date now = new Date();
        double amount = Math.random();
        orderEntity.setId(IdUtil.simpleUUID());
        orderEntity.setAmount(String.valueOf(amount));
        orderEntity.setCreateTime(now);
        String dateString = DateUtil.format(now,"yyyyMMddHHmmss");
        orderEntity.setOrderNum("CG_" + dateString + i);
        orderEntity.setStatus("10");
    }
}
