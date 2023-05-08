package com.sichao.blogService.controller;

import com.sichao.blogService.entity.vo.PublishTopicVo;
import com.sichao.blogService.service.BlogTopicService;
import com.sichao.common.utils.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
//@CrossOrigin//跨域解决方法(gateway网关的跨域配置与这个注解不要一起使用，会有出错)
@Tag(name = "话题模块")//将该Controller类下的接口放入knife4j中，并命名为“话题模块”
public class BlogTopicController {
    @Autowired
    private BlogTopicService blogTopicService;

    //发布话题
    @Operation(summary = "发布话题")
    @PostMapping("/publishTopic")
    public R publishTopic(@RequestBody PublishTopicVo publishTopicVo){
        System.out.println(publishTopicVo.getTopicTitle());
        System.out.println(publishTopicVo.getTopicDescription());
        System.out.println(publishTopicVo.getCreatorId());
        blogTopicService.publishTopic(publishTopicVo);
        return R.ok();
    }

    //查询热门话题




}
