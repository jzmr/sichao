package com.sichao.common.handle;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;

//mybatis-plus的自动填充配置类
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    //插入数据时填充
    @Override
    public void insertFill(MetaObject metaObject) {
        //注意：这里的fieldName对应的是实体类的实例变量（createTime），不是数据库表中的字段（create_time）
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    //修改数据时填充
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
