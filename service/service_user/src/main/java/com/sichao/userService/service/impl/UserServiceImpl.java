package com.sichao.userService.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sichao.common.constant.Constant;
import com.sichao.common.constant.PrefixKeyConstant;
import com.sichao.common.entity.to.userInfoTo;
import com.sichao.common.exceptionhandler.sichaoException;
import com.sichao.common.utils.JwtUtils;
import com.sichao.common.utils.MD5;
import com.sichao.common.utils.RandomSxpire;
import com.sichao.userService.entity.User;
import com.sichao.userService.entity.vo.RegisterVo;
import com.sichao.userService.entity.vo.UpdateInfoVo;
import com.sichao.userService.entity.vo.UpdatePasswordVo;
import com.sichao.userService.entity.vo.UserInfoVo;
import com.sichao.userService.mapper.UserMapper;
import com.sichao.userService.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author jicong
 * @since 2023-04-28
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    //保存在RedisTemplate与StringRedisTemplate之间的数据是隔离的
    //在默认情况下Java 8不支持LocalDateTime，所以除非在另外配置的情况下，不然不要使用RedisTemplate
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;


    //注册
    @Transactional(rollbackFor = Exception.class)//把事务交给spring管理
    @Override
    public void register(RegisterVo registerVo) {
        //获取注册的数据
        String nickname = registerVo.getNickname();//昵称
        String phone = registerVo.getPhone();//手机号
        String password = registerVo.getPassword();//密码
        String code = registerVo.getCode();//验证码

        //校验传入参数什么合法，不合法则抛出异常
        regexMatch(nickname, phone, password, code);

        //判断验证码是否一致 TODO
//        String redisCode = stringRedisTemplate.opsForValue().get(PrefixKeyConstant.SMS_CODE_PREFIX+phone);
//        if(!code.equals(redisCode)){
//            throw new sichaoException(Constant.FAILURE_CODE,"验证码错误，注册失败");
//        }
//        //判断昵称是否重复（开发环境下注释掉）TODO
//        QueryWrapper<User> wrapper=new QueryWrapper();
//        wrapper.eq("nickname", nickname);
//        User userOne = baseMapper.selectOne(wrapper);
//        if(userOne!=null){//说明昵称重复
//            throw new sichaoException(Constant.FAILURE_CODE,"昵称重复,注册失败");
//        }
//
//        //判断手机号是否重复（开发环境下注释掉）TODO
//        QueryWrapper<User> wrapper1=new QueryWrapper();
//        wrapper1.eq("phone",phone);
//        User userOne1 = baseMapper.selectOne(wrapper1);
//        if(userOne1!=null){//说明手机号重复
//            throw new sichaoException(Constant.FAILURE_CODE,"手机号重复,注册失败");
//        }

        //添加数据到数据库
        User user = new User();
        user.setPhone(phone);
        user.setPassword(MD5.saltEncryption(password));//将密码使用MD5盐值加密后保存到数据库中
        user.setNickname(nickname);
        user.setStatus(true);//用户可用

        //设置默认头像
        user.setAvatarUrl("http://thirdwx.qlogo.cn/mmopen/vi_32/DYAIOgq83eoj0hHXhgJNOTSOFsS4uZs8x1ConecaVOB8eIl115xmJZcT4oCicvia7wMEufibKtTLqiaJeanU2Lpg3w/132");
        baseMapper.insert(user);
    }

    //登录
    @Override
    public String login(User user) {
        //1、获取登录的数据
        String phone = user.getPhone();
        String password = user.getPassword();
        //2、校验传入参数什么合法，不合法则抛出异常
        regexMatch2(phone, password);
        //3、查找数据库是否有对应手机号的数据
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", phone);
        User userOne = baseMapper.selectOne(wrapper);
        //4、判断是否能登录成功
        if (userOne == null || !MD5.verify(password, userOne.getPassword())) {//MD5解密
            throw new sichaoException(Constant.FAILURE_CODE, "手机号或密码错误，登录失败");
        }
        //5、登录成功，使用JWT工具类生成token字符串
        //传入主体部分，生成有效期1天的token字符串的方法//传入id和昵称（注意，要在查了数据库生成的对象中取值）
        String jwtToken = JwtUtils.getJwtToken(String.valueOf(userOne.getId()), userOne.getNickname(),Constant.ACCESS_TOKEN_EXPIRE);
        //以前缀+token为key，将用户信息（id+nickname）放入redis中，过期时间为5天
        stringRedisTemplate.opsForValue().set(
                PrefixKeyConstant.USER_TOKEN_PREFIX+jwtToken,
                JSON.toJSONString(new userInfoTo(userOne.getId(), userOne.getNickname())),
                Constant.REFRESH_TOKEN_EXPIRE,
                TimeUnit.MILLISECONDS);

        return jwtToken;
    }
    //根据token信息获取用户信息（密码除外）
    @Override
    public UserInfoVo getUserInfoByToken(String id) {
        User user = baseMapper.selectById(id);
        UserInfoVo userInfo = new UserInfoVo();
        BeanUtils.copyProperties(user, userInfo);
        return userInfo;
    }

    //注销
    @Override
    public void logout(String token) {
        //获取token的剩余生存时间并加点余量避免因程序耗时出现空档期
        long tokenTTL = JwtUtils.getTokenTTL(token)+(int)(Math.random()*1000*60);
        //将token放入黑名单中（使用redis实现）
        stringRedisTemplate.opsForValue().set(PrefixKeyConstant.USER_BLACK_TOKEN_PREFIX + token,"",tokenTTL,TimeUnit.MILLISECONDS);
        //删除redis中续签token的key
        stringRedisTemplate.delete(PrefixKeyConstant.USER_TOKEN_PREFIX + token);
    }

    //查看用户是否被禁用
    @Override
    public boolean userIsDisabled(String userId) {
        boolean isDisabled=baseMapper.userIsDisabled(userId);//true为可用，false为禁用
        return !isDisabled;//取反
    }

    //修改密码，修改完成之后注销账号
    @Transactional
    @Override
    public void updatePassword(String token,String userId, UpdatePasswordVo updatePasswordVo) {
        //获取修改密码的数据
        String phone = updatePasswordVo.getPhone();//手机号
        String oldPassword = updatePasswordVo.getOldPassword();//原密码
        String newPassword = updatePasswordVo.getNewPassword();//新密码
        String code = updatePasswordVo.getCode();//验证码

        //校验传入参数什么合法，不合法则抛出异常
        regexMatch3(phone, oldPassword,newPassword, code);

        //判断验证码是否一致 TODO
//        String redisCode = stringRedisTemplate.opsForValue().get(PrefixKeyConstant.SMS_CODE_PREFIX + phone);
//        if(!code.equals(redisCode)){
//            throw new sichaoException(Constant.FAILURE_CODE,"验证码错误，修改密码失败");
//        }
        //判断原密码与新密码是否一致
        if(oldPassword.equals(newPassword)){
            throw new sichaoException(Constant.FAILURE_CODE,"原密码与新密码不能相同");
        }
        //判断原密码与数据库的密码是否一致
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("id",userId);
        wrapper.eq("phone",phone);
        wrapper.select("id","phone","password");
        User user = baseMapper.selectOne(wrapper);
        if(!MD5.verify(oldPassword, user.getPassword())){
            throw new sichaoException(Constant.FAILURE_CODE, "原密码错误，修改密码失败");
        }
        //修改密码
        user.setPassword(MD5.saltEncryption(newPassword));
        baseMapper.updateById(user);
        //注销原先的登录token
        try {
            this.logout(token);
        }catch (Exception e){
            throw new sichaoException(Constant.FAILURE_CODE,"修改密码失败，请重新修改密码");
        }
    }

    //根据用户id查看用户信息（密码除外）
    //需要加缓存，因为如果是大V则会有非常多的人查看ta的主页
    @Override
    public UserInfoVo getUserInfoById(String id) {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String userInfoKey = PrefixKeyConstant.USER_INFO_PREFIX + id;//用户信息key
        String userInfoLockKey = PrefixKeyConstant.USER_INFO_LOCK_PREFIX + id;//用户信息锁key
        String followerModifyKey = PrefixKeyConstant.USER_FOLLOWER_MODIFY_PREFIX + id;//用户粉丝变化数key
        String followingModifyKey = PrefixKeyConstant.USER_FOLLOWING_MODIFY_PREFIX + id;//用户关注变化数key

        UserInfoVo userInfo = JSON.parseObject(ops.get(userInfoKey), UserInfoVo.class);
        if(userInfo == null){
            RLock lock = redissonClient.getLock(userInfoLockKey);//锁key
            lock.lock();//加锁，阻塞
            try {
                //双查机制，在锁内再查一遍缓存中是否有数据
                userInfo = JSON.parseObject(ops.get(userInfoKey), UserInfoVo.class);
                if(userInfo == null){
                    //查询数据库
                    User user = baseMapper.selectById(id);
                    userInfo = new UserInfoVo();
                    BeanUtils.copyProperties(user, userInfo);
                    ops.set(userInfoKey,JSON.toJSONString(userInfo),
                            Constant.ONE_HOURS_EXPIRE + RandomSxpire.getRandomSxpire(),
                            TimeUnit.MILLISECONDS);//缓存到redis
                }
            }finally {
                lock.unlock();//解锁
            }
        }
        //查询用户关注数、粉丝数、博客数、获赞数 TODO
        //保存在数据库中的关注数与粉丝数不是实时的数据，要加上redis中的变化数，之后会使用定时任务落盘数据到msyql并清除变化数缓存
        String followerModifyCount = ops.get(followerModifyKey);
        if(followerModifyCount != null){//加上粉丝变化数
            userInfo.setFollowerCount(userInfo.getFollowerCount()+Integer.parseInt(followerModifyCount));
        }
        String followingModifyCount = ops.get(followingModifyKey);
        if(followingModifyCount!=null){//加上关注变化数
            userInfo.setFollowingCount((short) (userInfo.getFollowingCount()+Integer.parseInt(followingModifyCount)));
        }
        return userInfo;
    }
    //修改头像url
    @Override
    public void updateAvatarUrl(String userId, String avatarUrl) {
        User user = new User();
        user.setId(userId);
        user.setAvatarUrl(avatarUrl);
        baseMapper.updateById(user);
        stringRedisTemplate.delete(PrefixKeyConstant.USER_INFO_PREFIX + userId);//删除用户信息缓存
    }

    //修改用户个人信息（头像、密码除外）
    @Override
    public void updateInfo(String userId, UpdateInfoVo updateInfoVo) {
        //创建模版对象(这里的正则表达式不需要带斜杠“/”)
        Pattern p = Pattern.compile("^.{2,8}$");//校验昵称/^.{2,8}$/
        Matcher m = p.matcher(updateInfoVo.getNickname());//进行匹配，//m.find()为true什么匹配，为false说明不匹配
        if (!m.find()) throw new sichaoException(Constant.FAILURE_CODE, "昵称格式不正确，昵称最少2位、最多8位");
        //修改信息
        User user = new User();
        BeanUtils.copyProperties(updateInfoVo,user);//对象拷贝（源，目标）
        user.setId(userId);
        baseMapper.updateById(user);

        //删除用户信息缓存
        String userInfoKey = PrefixKeyConstant.USER_INFO_PREFIX + userId;//用户信息key
        stringRedisTemplate.delete(userInfoKey);
    }


    //校验传入参数什么合法：昵称，手机号，密码，验证码
    public void regexMatch(String nickname, String phone, String password, String code) {
        //做非空判断(之所以在后端做非空判断而不在前端做，是因为前端的数据不一定可靠，任意被恶意篡改)
        if (!StringUtils.hasText(code) || !StringUtils.hasText(phone) ||
                !StringUtils.hasText(nickname) || !StringUtils.hasText(password)) {
            throw new sichaoException(Constant.FAILURE_CODE, "注册数据为空，注册失败");
        }

        //创建模版对象(这里的正则表达式不需要带斜杠“/”)
        Pattern p = Pattern.compile("^1[3-9]\\d{9}$");//校验手机号(匹配大陆地区手机号)/^1[3-9]\d{9}$/
        Matcher m = p.matcher(phone);//进行匹配，//m.find()为true什么匹配，为false说明不匹配
        if (!m.find()) throw new sichaoException(Constant.FAILURE_CODE, "手机号码格式不正确");

        p = Pattern.compile("^.{2,8}$");//校验昵称/^.{2,8}$/
        m = p.matcher(nickname);
        if (!m.find()) throw new sichaoException(Constant.FAILURE_CODE, "昵称格式不正确，昵称最少2位、最多8位");

        p = Pattern.compile("^[0-9]{6}$");//校验验证码/^[0-9]{6}$/
        m = p.matcher(code);
        if (!m.find()) throw new sichaoException(Constant.FAILURE_CODE, "验证码格式不正确，验证码为6位数字");

        p = Pattern.compile("^.{8,20}$");//校验密码/^.{8,20}$/
        m = p.matcher(password);
        if (!m.find()) throw new sichaoException(Constant.FAILURE_CODE, "密码格式不正确，密码至少为8位、最多20位");
    }

    //校验传入参数什么合法：手机号，密码
    public void regexMatch2(String phone, String password) {
        //做非空判断(之所以在后端做非空判断而不在前端做，是因为前端的数据不一定可靠，任意被恶意篡改)
        if (!StringUtils.hasText(phone) || !StringUtils.hasText(password)) {
            throw new sichaoException(Constant.FAILURE_CODE, "登录数据为空，登录失败");
        }
        //创建模版对象(这里的正则表达式不需要带斜杠“/”)
        Pattern p = Pattern.compile("^1[3-9]\\d{9}$");//校验手机号(匹配大陆地区手机号)/^1[3-9]\d{9}$/
        Matcher m = p.matcher(phone);//进行匹配，//m.find()为true什么匹配，为false说明不匹配
        if (!m.find()) throw new sichaoException(Constant.FAILURE_CODE, "手机号码格式不正确");

        p = Pattern.compile("^.{8,20}$");//校验密码/^.{8,20}$/
        m = p.matcher(password);
        if (!m.find()) throw new sichaoException(Constant.FAILURE_CODE, "密码格式不正确，密码至少为8位、最多20位");
    }

    //校验传入参数什么合法：手机号，原密码，新密码，验证码
    public void regexMatch3(String phone, String oldPassword, String newPassword, String code) {
        //做非空判断(之所以在后端做非空判断而不在前端做，是因为前端的数据不一定可靠，任意被恶意篡改)
        if (!StringUtils.hasText(code) || !StringUtils.hasText(phone) ||
                !StringUtils.hasText(oldPassword) || !StringUtils.hasText(newPassword)) {
            throw new sichaoException(Constant.FAILURE_CODE, "数据为空，修改密码失败");
        }

        //创建模版对象(这里的正则表达式不需要带斜杠“/”)
        Pattern p = Pattern.compile("^1[3-9]\\d{9}$");//校验手机号(匹配大陆地区手机号)/^1[3-9]\d{9}$/
        Matcher m = p.matcher(phone);//进行匹配，//m.find()为true什么匹配，为false说明不匹配
        if (!m.find()) throw new sichaoException(Constant.FAILURE_CODE, "手机号码格式不正确");

        p = Pattern.compile("^[0-9]{6}$");//校验验证码/^[0-9]{6}$/
        m = p.matcher(code);
        if (!m.find()) throw new sichaoException(Constant.FAILURE_CODE, "验证码格式不正确，验证码为6位数字");

        p = Pattern.compile("^.{8,20}$");//校验密码/^.{8,20}$/
        m = p.matcher(oldPassword);
        if (!m.find()) throw new sichaoException(Constant.FAILURE_CODE, "密码格式不正确，密码至少为8位、最多20位");

        p = Pattern.compile("^.{8,20}$");//校验密码/^.{8,20}$/
        m = p.matcher(newPassword);
        if (!m.find()) throw new sichaoException(Constant.FAILURE_CODE, "密码格式不正确，密码至少为8位、最多20位");
    }


}
