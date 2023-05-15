package com.sichao.userService.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.sichao.common.constant.Constant;
import com.sichao.common.constant.PrefixKeyConstant;
import com.sichao.common.exceptionhandler.sichaoException;
import com.sichao.userService.entity.User;
import com.sichao.userService.entity.UserFollow;
import com.sichao.userService.entity.vo.FollowListVo;
import com.sichao.userService.mapper.UserFollowMapper;
import com.sichao.userService.service.UserFollowService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sichao.userService.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * <p>
 * 用户关注用户关系id 服务实现类
 * </p>
 *
 * @author jicong
 * @since 2023-05-03
 */
@Service
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow> implements UserFollowService {
    @Autowired
    public UserService userService;//用户服务类
    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    // 关注用户
    @Transactional
    @Override
    public void follow(String userId, String followingId) {
        //查询
        User byId = userService.getById(followingId);
        if(byId==null)throw new sichaoException(Constant.FAILURE_CODE,"关注用户不存在");

        QueryWrapper<UserFollow> wrapper = new QueryWrapper<>();
        wrapper.eq("follower_id",userId);
        wrapper.eq("following_id",followingId);
        UserFollow one = baseMapper.selectOne(wrapper);
        int isSuccess=0;
        if(one!=null){//存在记录，修改状态
            if(!one.getStatus()){//未关注,则修改状态未关注
                one.setStatus(true);
                one.setUpdateTime(null);//避免修改时间自动填充失效
                isSuccess = baseMapper.updateById(one);//返回操作数据条目数：1
            }
        }else {//不存在记录，插入数据
            UserFollow follow = new UserFollow();
            follow.setFollowerId(userId);
            follow.setFollowingId(followingId);
            isSuccess = baseMapper.insert(follow);//返回操作数据条目数：1
        }

        //关注成功
        if(isSuccess == 1){
            ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
            String followerModifyKey = PrefixKeyConstant.USER_FOLLOWER_MODIFY_PREFIX + followingId;//用户粉丝变化数key
            String followingModifyKey = PrefixKeyConstant.USER_FOLLOWING_MODIFY_PREFIX + userId;//用户关注变化数key

            //关注人的关注用户数+1
            ops.increment(followingModifyKey);//自增，如果key不存在，则先创建整个key且值为0，而后再自增
            //被关注人的粉丝数+1
            ops.increment(followerModifyKey);
        }
    }
    @Transactional
    //取关用户
    @Override
    public void unfollow(String userId, String followingId) {
        //查询
        User byId = userService.getById(followingId);
        if(byId==null)throw new sichaoException(Constant.FAILURE_CODE,"关注用户不存在");

        QueryWrapper<UserFollow> wrapper = new QueryWrapper<>();
        wrapper.eq("follower_id",userId);
        wrapper.eq("following_id",followingId);
        UserFollow one = baseMapper.selectOne(wrapper);
        int isSuccess=0;
        if(one!=null){//存在记录，修改状态
            if(one.getStatus()){//已关注,则修改状态未关注
                one.setStatus(false);
                one.setUpdateTime(null);//避免修改时间自动填充失效
                isSuccess = baseMapper.updateById(one);//返回操作数据条目数：1
            }
        }
        //取关成功
        if(isSuccess==1){
            ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
            String followerModifyKey = PrefixKeyConstant.USER_FOLLOWER_MODIFY_PREFIX + followingId;//用户粉丝变化数key
            String followingModifyKey = PrefixKeyConstant.USER_FOLLOWING_MODIFY_PREFIX + userId;//用户关注变化数key

            //关注人的关注用户数-1
            ops.decrement(followingModifyKey);//自减，如果key不存在，则先创建整个key且值为0，而后再自减
            //被关注人的粉丝数-1
            ops.decrement(followerModifyKey);
        }
    }

    // 查看当前用户是否关注某位其他用户
    @Override
    public boolean getFollowStatus(String userId, String id) {
        QueryWrapper<UserFollow> wrapper = new QueryWrapper<>();
        wrapper.eq("follower_id",userId);//粉丝id
        wrapper.eq("following_id",id);//被关注者id
        wrapper.select("status");
        UserFollow userFollow = baseMapper.selectOne(wrapper);
        if(userFollow == null)return false;
        return userFollow.getStatus();
    }
    //查看用户关注列表
    @Override
    public List<FollowListVo> getFollowingList(String currentId, String id) {
        List<FollowListVo> list = baseMapper.getFollowingList(currentId,id);
        return list;
    }
    //查看用户粉丝列表
    @Override
    public List<FollowListVo> getFollowerList(String currentId, String id) {
        List<FollowListVo> list = baseMapper.getFollowerList(currentId,id);
        return list;
    }

    //查询当前用户关注的用户的昵称
    @Override
    public List<String> getFollowingNicknameList(String userId) {
        List<String> nicknameList = baseMapper.getFollowingNicknameList(userId);
        return nicknameList;
    }

}
