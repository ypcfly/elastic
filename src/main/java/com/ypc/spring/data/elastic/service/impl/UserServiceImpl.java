package com.ypc.spring.data.elastic.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.math.MathUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.ypc.spring.data.elastic.dto.QueryDTO;
import com.ypc.spring.data.elastic.entity.OrderEntity;
import com.ypc.spring.data.elastic.entity.UserEntity;
import com.ypc.spring.data.elastic.service.UserService;
import lombok.extern.slf4j.Slf4j;

import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.omg.PortableInterceptor.LOCATION_FORWARD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    private static final String USER_INDEX_NAME = "user_entity";

//    private UserRepository userRepository;

//    @Autowired
//    private ElasticsearchRestTemplate elasticsearchRestTemplate;

//    @Autowired
//    private ElasticsearchOperations elasticsearchOperations;
//
//    public UserServiceImpl(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }

//    @Override
//    public Page<UserEntity> pageQuery(QueryDTO queryDTO) {
//        // 分页默认从0开始，按照userGrade逆向排序
//        PageRequest pageRequest = PageRequest.of(queryDTO.getPageNum() - 1,queryDTO.getPageSize(), Sort.by(Sort.Direction.DESC,"userAge"));
//        Page<UserEntity> page = null;
//        // 条件查询
//        if (Boolean.TRUE.equals(queryDTO.getCondition())) {
//            Integer min = queryDTO.getMinAge();
//            Integer max = queryDTO.getMaxAge();
//            String userCode = queryDTO.getUserCode();
//            page = userRepository.queryPage(userCode,min,max,pageRequest);
//        } else {
//            // 查询所有
//            page = userRepository.findAll(pageRequest);
//        }
//        return page;
//    }
//
//    @Override
//    public UserEntity save(UserEntity userEntity) {
//        List<OrderEntity> orderEntityList = new ArrayList<>();
//        String userId = IdUtil.simpleUUID();
//        // 自定义Id覆盖
//        userEntity.setId(userId);
//        for (int i = 0; i < 4; i++) {
//            OrderEntity orderEntity = new OrderEntity();
//            setProperties(orderEntity,i);
//            orderEntity.setUserId(userId);
//            orderEntityList.add(orderEntity);
//        }
//        userEntity.setOrderEntityList(orderEntityList);
//        return userRepository.save(userEntity);
//    }

//    private String updateUser(UserEntity userEntity) {
//        log.info(">>>> user={} <<<<",userEntity);
//        Map<String,Object> paramMap = new HashMap<>();
//
//        paramMap.put("id",userEntity.getId());
//        paramMap.put("userMobile",userEntity.getUserMobile());
//        UpdateQuery updateQuery = UpdateQuery.builder(userEntity.getId()).withDocument(Document.from(paramMap)).build();
//        UpdateResponse updateResponse = elasticsearchRestTemplate.update(updateQuery, IndexCoordinates.of("user_entity_index"));
//        UpdateResponse.Result result = updateResponse.getResult();
//        return String.valueOf(result);
//    }

//    @Override
//    public UserEntity queryById(String id) {
//        Optional<UserEntity> optional = userRepository.findById(id);
//        return optional.isPresent() ? optional.get() : null;
//    }

//    @Override
//    public void deleteById(String id) {
//        userRepository.deleteById(id);
//    }

//    @Override
//    public UserEntity update(UserEntity userEntity) {
////        Optional<UserEntity> optional = userRepository.findById(userEntity.getId());
////        if (!optional.isPresent()) {
////            return null;
////        }
////        List<OrderEntity> orderEntityList = optional.get().getOrderEntityList();
////        userEntity.setOrderEntityList(orderEntityList);
////        return userRepository.save(userEntity);
//
//        String result = updateUser(userEntity);
//        Optional<UserEntity> optional = userRepository.findById(userEntity.getId());
//        return optional.isPresent() ? optional.get() : null;
//    }

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

    @Override
    public Page<UserEntity> pageQuery(QueryDTO queryDTO) {
        String[] includes = {"userName","id","userCode","userMobile","userGrade","status"};
        // 分页默认从0开始，按照userGrade逆向排序
        PageRequest pageRequest = PageRequest.of(queryDTO.getPageNum() - 1,queryDTO.getPageSize(), Sort.by(Sort.Direction.DESC,"userAge"));
        Page<UserEntity> page = null;
        // 条件查询
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery().mustNot(QueryBuilders.matchQuery("status","00"));
        if (StrUtil.isNotBlank(queryDTO.getUserCode())) {
            queryBuilder.must(QueryBuilders.termQuery("userCode",queryDTO.getUserCode()));
        }
        int pageNum = queryDTO.getPageNum() - 1;
        int pageSize = queryDTO.getPageSize();
        SearchSourceBuilder searchSourceBuilder = SearchSourceBuilder.searchSource().fetchSource(includes,null)
                .query(queryBuilder).sort("userGrade", SortOrder.DESC)
                .from(pageNum * pageSize).size(pageSize);
        SearchRequest searchRequest = new SearchRequest(USER_INDEX_NAME).source(searchSourceBuilder);

        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest,RequestOptions.DEFAULT);
            long total = searchResponse.getHits().getTotalHits().value;
            SearchHit[] searchHits = searchResponse.getHits().getHits();
            List<UserEntity> records = convertSource2List(searchHits);
            page = new PageImpl(records,pageRequest,total);
        } catch (IOException e) {
            log.error(">>>> 查询失败，异常信息={} <<<<",e.getMessage());
        }
        return page;
    }

    private List<UserEntity> convertSource2List(SearchHit[] searchHits) {
        if (ArrayUtil.isEmpty(searchHits)) {
            return Collections.EMPTY_LIST;
        }
        List<UserEntity> resultList = new ArrayList<>(searchHits.length);
        for (SearchHit hit : searchHits) {
            String jsonString = hit.getSourceAsString();
            UserEntity userEntity = JSONUtil.toBean(jsonString,UserEntity.class);
            resultList.add(userEntity);
        }
        return resultList;
    }

    @Override
    public UserEntity save(UserEntity userEntity) {
        String id = IdUtil.simpleUUID();
        userEntity.setId(id);
        List<OrderEntity> orderEntityList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            OrderEntity orderEntity = new OrderEntity();
            setProperties(orderEntity,i);
            orderEntity.setUserId(id);
            orderEntityList.add(orderEntity);
        }
        userEntity.setOrderEntityList(orderEntityList);
        Map<String,Object> sourceMap = createSourceMap(userEntity);
        IndexRequest indexRequest = new IndexRequest(USER_INDEX_NAME).opType(DocWriteRequest.OpType.CREATE)
                .id(id).source(sourceMap);
        try {
            IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
            System.out.println(indexResponse.status().getStatus());
            if (indexResponse.status().getStatus() != RestStatus.CREATED.getStatus()) {
                log.error(">>>> 新增数据失败，返回结果状态码={}，错误信息={} <<<<",indexResponse.status().getStatus());
            }
        } catch (IOException e) {
            log.error(">>>> 新增数据出现异常，异常信息={} <<<<",e.getMessage());
        }
        return userEntity;
    }

    private Map<String, Object> createSourceMap(UserEntity userEntity) {
        Map<String,Object> beanMap =  BeanUtil.beanToMap(userEntity,false,true);
        List<Map<String,Object>> nestedMap = createNestedMap(userEntity.getOrderEntityList());
        Map<String,Object> resultMap = new HashMap<>();
        BeanUtil.copyProperties(beanMap,resultMap,"orderEntityList");
        resultMap.put("orderEntityList",nestedMap);
        return resultMap;
    }

    private List<Map<String, Object>> createNestedMap(List<OrderEntity> orderEntityList) {
        if (CollUtil.isEmpty(orderEntityList)) {
            return Collections.EMPTY_LIST;
        }
        List<Map<String,Object>> list = new ArrayList<>(orderEntityList.size());
        String format = "yyyy-MM-dd HH:mm:ss";
        for (OrderEntity orderEntity : orderEntityList) {
            Map<String,Object> beanMap = BeanUtil.beanToMap(orderEntity,false,true);
            Date createTime = (Date) beanMap.get("createTime");
            if (Objects.nonNull(createTime)) {
                beanMap.put("createTime",DateUtil.format(createTime,format));
            }
            Date updateTime = (Date) beanMap.get("updateTime");
            if (Objects.nonNull(updateTime)) {
                beanMap.put("updateTime",DateUtil.format(updateTime,format));
            }
            list.add(beanMap);
        }
        return list;
    }

    @Override
    public UserEntity queryById(String id) {
        GetRequest getRequest = new GetRequest(USER_INDEX_NAME).id(id);
        UserEntity userEntity = null;
        try {
            GetResponse getResponse = restHighLevelClient.get(getRequest,RequestOptions.DEFAULT);
            Map<String,Object> map = getResponse.getSource();
            userEntity = BeanUtil.mapToBean(map,UserEntity.class,false,CopyOptions.create());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return userEntity;
    }

    @Override
    public void deleteById(String id) {
        DeleteRequest deleteRequest = new DeleteRequest(USER_INDEX_NAME).id(id);
        try {
            DeleteResponse deleteResponse = restHighLevelClient.delete(deleteRequest,RequestOptions.DEFAULT);
            if (deleteResponse.status().getStatus() != RestStatus.OK.getStatus()) {
                log.error(">>>> 删除id={}数据失败，返回状态码={} <<<<",id,deleteResponse.status().getStatus());
            }
        } catch (IOException e) {
            log.error(">>>> 删除数据发生异常，id={}，异常信息={} <<<<",id,e.getMessage());
        }
    }

    @Override
    public UserEntity update(UserEntity userEntity) {
        String id = userEntity.getId();
        Map<String,Object> sourceMap = BeanUtil.beanToMap(userEntity,false,true);
        if (CollUtil.isNotEmpty(userEntity.getOrderEntityList())) {
            sourceMap.put("orderEntityList",createNestedMap(userEntity.getOrderEntityList()));
        }
        try {
            UpdateRequest updateRequest = new UpdateRequest(USER_INDEX_NAME,id).doc(sourceMap);
            UpdateResponse updateResponse = restHighLevelClient.update(updateRequest,RequestOptions.DEFAULT);
            if (updateResponse.status().getStatus() != RestStatus.OK.getStatus()) {
                log.error(">>>> 修改id={}数据失败，返回状态码={} <<<<",id,updateResponse.status().getStatus());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return queryById(userEntity.getId());
    }
}
