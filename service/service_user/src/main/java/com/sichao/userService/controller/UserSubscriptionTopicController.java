package com.sichao.userService.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 用户订阅话题关系表 前端控制器
 * </p>
 *
 * @author jicong
 * @since 2023-04-28
 */
@RestController
@RequestMapping("/userService/userSubscriptionTopic")
//@CrossOrigin//跨域解决方法
@Tag(name = "话题订阅模块")//将该Controller类下的接口放入knife4j中，并命名为“话题订阅模块”
public class UserSubscriptionTopicController {

}
