package com.sichao.blogService.controller;

import com.sichao.blogService.entity.vo.PublishTopicVo;
import com.sichao.blogService.entity.vo.TopicInfoVo;
import com.sichao.blogService.entity.vo.TopicTitleVo;
import com.sichao.blogService.service.BlogTopicService;
import com.sichao.common.interceptor.TokenRefreshInterceptor;
import com.sichao.common.utils.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 话题表 前端控制器
 * </p>
 *
 * @author jicong
 * @since 2023-04-29
 */
@RestController
@RequestMapping("/blogService/blogTopic")
@Tag(name = "话题模块")
public class BlogTopicController {
    @Autowired
    private BlogTopicService blogTopicService;

    //发布话题
    @Operation(summary = "发布话题")
    @PostMapping("/publishTopic")
    public R publishTopic(@RequestBody PublishTopicVo publishTopicVo){
        //threadLocal中无数据时说明未登录
        HashMap<String, String> map = TokenRefreshInterceptor.threadLocal.get();
        if(map==null)return R.error().message("未登录");

        publishTopicVo.setCreatorId(map.get("userId"));
        blogTopicService.publishTopic(publishTopicVo);
        return R.ok();
    }

    //查询热门话题（热搜榜）
    @Operation(summary = "查询热门话题（热搜榜）")
    @GetMapping("/getHotTopicList")
    public R getHotTopicList(){
        //查询包含话题id与话题title的话题标签集合list
        List<TopicTitleVo> hotTopicList = blogTopicService.getHotTopicList();
        return R.ok().data("hotTopicList",hotTopicList);
    }

    //获取某个话题的信息
    @Operation(summary = "获取某个话题的信息")
    @GetMapping("/getTopicInfo/{topicId}")
    public R getTopicInfo(@PathVariable String topicId){
        TopicInfoVo topicInfo =blogTopicService.getTopicInfo(topicId);
        return R.ok().data("topicInfo",topicInfo);
    }
    //TODO
    //修改某个话题的信息

    //给某个话题增加热度（权重）

    //禁用话题
    @Operation(summary = "禁用话题")
    @PostMapping("/forbiddenTopicById")
    public R forbiddenTopicById(String id,String topicTitle){
        blogTopicService.forbiddenTopicById(id,topicTitle);
        return R.ok();
    }
    //启用话题
    @Operation(summary = "启用话题")
    @PostMapping("/enableTopicById")
    public R enableTopicById(String id,String topicTitle){
        blogTopicService.enableTopicById(id,topicTitle);
        return R.ok();
    }
    //审查话题



}
