package com.sichao.userService.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sichao.common.interceptor.TokenRefreshInterceptor;
import com.sichao.common.utils.R;
import com.sichao.userService.entity.UserFollow;

import com.sichao.userService.entity.vo.FollowListVo;
import com.sichao.userService.service.UserFollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 用户关注用户关系id 前端控制器
 * </p>
 *
 * @author jicong
 * @since 2023-05-03
 *
 * follow       关注
 * follower     粉丝，即发起关注的用户
 * following    被关注人,即被关注的用户
 */
@RestController
@RequestMapping("/userService/userFollow")
//@CrossOrigin//跨域解决方法
@Tag(name = "用户关注模块")//将该Controller类下的接口放入knife4j中，并命名为“用户关注模块”
public class UserFollowController {
    @Autowired
    private UserFollowService userFollowService;

    // 关注用户
    @Operation(summary = "关注用户")
    @PostMapping("/follow/{followingId}")//传入要关注的id
    public R follow(@PathVariable("followingId") String followingId){
        //threadLocal中无数据时说明未登录
        HashMap<String, String> map = TokenRefreshInterceptor.threadLocal.get();
        if(map==null)return R.error().message("未登录");
        userFollowService.follow(map.get("userId"),followingId);
        return R.ok();
    }
    //取关用户
    @Operation(summary = "取关用户")
    @PostMapping("/unfollow/{followingId}")//传入要取关的id
    public R unfollow(@PathVariable("followingId") String followingId){
        //threadLocal中无数据时说明未登录
        HashMap<String, String> map = TokenRefreshInterceptor.threadLocal.get();
        if(map==null)return R.error().message("未登录");
        userFollowService.unfollow(map.get("userId"),followingId);
        return R.ok();
    }

    // 查看当前用户是否关注某位其他用户
    @Operation(summary = "查看当前用户是否关注某位其他用户")
    @GetMapping("/getFollowStatus/{id}")//传入要关注的id
    public R getFollowStatus(@PathVariable("id") String id){
        //threadLocal中无数据时说明未登录
        HashMap<String, String> map = TokenRefreshInterceptor.threadLocal.get();
        if(map==null)return R.ok().data("isFollow",false);//未登录，返回未关注
        boolean isFollow=userFollowService.getFollowStatus(map.get("userId"),id);
        return R.ok().data("isFollow",isFollow);
    }

    // 分页查看用户关注列表，倒序
    @Operation(summary = "查看用户关注列表")
    @GetMapping("/getFollowingList/{id}/{page}/{limit}")//传入要查看关注列表的用户id，页码，每页条目数
    public R getFollowingList(@PathVariable("id") String id,@PathVariable("page") int page,@PathVariable("limit") int limit){
        //threadLocal中有数据时说明未登录
        HashMap<String, String> map = TokenRefreshInterceptor.threadLocal.get();
        String currentId = null;//当前用户id
        if(map!=null)currentId=map.get("userId");

        //①使用mybatis-plus自带的分页功能，（缺点：只能对有数据表的实体类对象进行分页，优点：可用比较轻松的进行带条件分页查询）
        //Page<User> page = new Page<>(1,5);//指定想要查询的页数与每页数据条数
        //userMapper.selectPage(page, null);//执行查询
        //②PageHelper分页，需要另外引入依赖，简单，只要是集合都能进行分页
        //这一行必须在需要分页的数据集合上面
        PageHelper.startPage(page, limit);
        List<FollowListVo> followingList=userFollowService.getFollowingList(currentId,id);
        //PageInfo就是一个分页的Bean，里面存放的是分页的各个信息，包括分页后的数据、页码、页数、每页长度。。。等等信息
        PageInfo pageInfo=new PageInfo(followingList);
//        System.out.println(pageInfo.getSize());//每页长度当前页长度
//        System.out.println(pageInfo.getPageNum());//当前页码
//        System.out.println(pageInfo.getTotal());//总记录数
//        System.out.println(pageInfo.getPages());//总页数
//        System.out.println(pageInfo.getPageSize());//每页最大长度
//        System.out.println(pageInfo.getList());//当前页数据
//        System.out.println(pageInfo.getPrePage());//上一页页码
//        System.out.println(pageInfo.getNextPage());//下一页页码
        return R.ok().data("followingList",followingList).data("pageInfo",pageInfo);
    }
    // 分页查看用户粉丝列表，倒序
    @Operation(summary = "查看用户粉丝列表")
    @GetMapping("/getFollowerList/{id}/{page}/{limit}")//传入要查看粉丝列表的用户id，页码，每页条目数
    public R getFollowerList(@PathVariable("id") String id,@PathVariable("page") int page,@PathVariable("limit") int limit){
        //threadLocal中有数据时说明未登录
        HashMap<String, String> map = TokenRefreshInterceptor.threadLocal.get();
        String currentId = null;//当前用户id
        if(map!=null)currentId=map.get("userId");
        //分页查询
        PageHelper.startPage(page, limit);
        List<FollowListVo> followerList=userFollowService.getFollowerList(currentId,id);
        PageInfo pageInfo=new PageInfo(followerList);
        return R.ok().data("followerList",followerList).data("pageInfo",pageInfo);
    }

    //查询当前用户关注的用户的昵称 TODO 可以做成互动多的或者最近获得的用户向查出来
    @Operation(summary = "分页查询当前用户关注的用户的昵称")
    @GetMapping("/getFollowingNicknameList/{page}/{limit}")//TODO 可以改成通过搜索模糊查询
    public R getFollowingNicknameList(@PathVariable("page") int page,@PathVariable("limit") int limit){
        //threadLocal中无数据时说明未登录
        HashMap<String, String> map = TokenRefreshInterceptor.threadLocal.get();
        if(map==null)return R.error().message("未登录");

        PageHelper.startPage(page, limit);
        List<String> nicknameList = userFollowService.getFollowingNicknameList(map.get("userId"));
        return R.ok().data("nicknameList",nicknameList);
    }
    //获取用户关注列表(用于远程调用)
    @Operation(summary = "获取用户关注列表")
    @GetMapping("/getFollowingSetCache")
    public R getFollowingSetCache(@RequestParam("userId") String userId){
        Set<String> followingSet = userFollowService.getFollowingSetCache(userId);
        return R.ok().data("followingSet",followingSet);
    }

    //获取用户粉丝列表(用于远程调用)
    @Operation(summary = "获取用户粉丝列表")
    @GetMapping("/getFollowerSetCache")
    public R getFollowerSetCache(@RequestParam("userId") String userId){
        Set<String> followerSet = userFollowService.getFollowerSetCache(userId);
        return R.ok().data("followerSet",followerSet);
    }


    // 查看用户关注量、粉丝量,
    // @自己的关注用户并发送通知。TODO

}
