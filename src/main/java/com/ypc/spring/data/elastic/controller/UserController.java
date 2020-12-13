package com.ypc.spring.data.elastic.controller;

import cn.hutool.core.util.StrUtil;
import com.ypc.spring.data.elastic.dto.QueryDTO;
import com.ypc.spring.data.elastic.entity.UserEntity;
import com.ypc.spring.data.elastic.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/pageQuery")
    public ResponseEntity<Page<UserEntity>> pageQuery(@RequestBody QueryDTO queryDTO) {
        Page<UserEntity> page = userService.pageQuery(queryDTO);
        return ResponseEntity.ok(page);
    }

    @PostMapping("/save")
    public ResponseEntity<UserEntity> save(@RequestBody UserEntity userEntity) {
        UserEntity result = null;
        if (StrUtil.isBlank(userEntity.getId())) {
            result = userService.save(userEntity);
        } else {
            result = userService.update(userEntity);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/queryById/{id}")
    public ResponseEntity<UserEntity> queryById(@PathVariable String id) {
        UserEntity result = userService.queryById(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/deleteById/{id}")
    public ResponseEntity<String> deleteById(@PathVariable String id) {
        userService.deleteById(id);
        return ResponseEntity.ok("success");
    }
}
