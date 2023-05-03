package com.sichao.service_blog;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.converts.MySqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.querys.MySqlQuery;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.keywords.MySqlKeyWordsHandler;
import org.junit.Test;

//mybatis-plus3.5版本的新版代码生成器
public class CodeGenerator {
    @Test
    public void generator() {
        //数据库配置 使用官网默认
        DataSourceConfig dataSourceConfig = new DataSourceConfig.Builder("jdbc:mysql://127.0.0.1:3306/sichao_blog?serverTimezone=GMT%2B8", "root", "1234")
                .dbQuery(new MySqlQuery())//数据库查询
                .typeConvert(new MySqlTypeConvert())//数据库类型转换器
                .keyWordsHandler(new MySqlKeyWordsHandler())//数据库关键字处理
                .build();
        //全局配置，
        GlobalConfig globalConfig = new GlobalConfig.Builder()
                .fileOverride()//覆盖已生成的文件，默认false
                .disableOpenDir()//禁止加载完之后打开
                .outputDir("F:\\project\\java_project\\sichao_parent\\service\\service_blog\\src\\main\\java")//输出指定目录默认
                .author("jicong")//作者
                //.enableKotlin()//是否开启kotlin模式 默认false
                .enableSwagger()//开启swaggerm模式。默认false
                .dateType(DateType.TIME_PACK) //时间策略DateType.ONLY_DATE 默认值: DateType.TIME_PACK
                .commentDate("yyyy-MM-dd")//注释的日期
                .build();
        //包配置
        PackageConfig packageConfig = new PackageConfig.Builder()
                .parent("com.sichao")//父包名
                .moduleName("blogService")//模块名
                .entity("entity")//实体类名称
                .service("service")//service包
//                .serviceImpl("service.impl.tt")//排至impl接口实现存放位置
                .mapper("mapper")
//                .xml("mapper.xml")
//                .controller("controller.hh")//同serviceImpl
//                .other("other")//其他包
                .build();
        //策略模式
        StrategyConfig strategyConfig = new StrategyConfig.Builder()
//                .enableCapitalMode()//开启大写命名
                .enableSkipView()//开启跳过视图
//                .disableSqlFilter()//禁用sql过滤
//                .likeTable(new LikeTable("USER"))
//                .addInclude("t_simple")//增加表的匹配
                .addTablePrefix("t_")//去除表的前缀如果t_user则变成User而不包含TUser
//                .addFieldPrefix("o")//增加字段过滤的前缀
//                .addFieldSuffix("_flag")//增加字段过滤前缀

                //实体类策略<------entity层--------->
                .entityBuilder()
                .enableLombok()//开启lombok
//              去下划线，NamingStrategy.no_change不发生变化，NamingStrategy.underline_to_camel开启驼峰命名
                .naming(NamingStrategy.underline_to_camel)
                .logicDeleteColumnName("isDelete")//逻辑删除字段数据库字段
                .enableTableFieldAnnotation()//开启开启实体生成时字段注解
                .idType(IdType.ASSIGN_ID)//设置主键类型


//                <-----controller层----->
                .controllerBuilder()
                .formatFileName("%sController")//controller层的拼接比如实体类时User拼接成UserController
                .enableRestStyle()//开启rest风格，主要为加了@RestController
                .enableHyphenStyle()//开启驼峰命名

//                <------service层------------>
                .serviceBuilder()
                .formatServiceFileName("I%sService")//同理Controller拼接文件名字

//                <-----mapper层控制-------->
                .mapperBuilder()
                .enableMapperAnnotation()//开启开启 @Mapper 注解，也可用MapperSacan代替
                .build();

        new AutoGenerator(dataSourceConfig)
                .global(globalConfig)
                .packageInfo(packageConfig)
                .strategy(strategyConfig)
                .execute();
    }
}

