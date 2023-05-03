package com.sichao.userService;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sichao.userService.entity.User;
import com.sichao.userService.entity.vo.RegisterVo;
import com.sichao.userService.mapper.UserMapper;
import com.sichao.userService.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
class ServiceUserApplicationTests {

    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;

    @Test
    void contextLoads() {
        RegisterVo registerVo = new RegisterVo();
        registerVo.setNickname("ki2i");
        registerVo.setPhone("13437544720");
        registerVo.setPassword("q23132");
        registerVo.setCode("123");
        userService.register(registerVo);
    }

    //测试 乐观锁插件
    @Test
    public void testOptimisticLocker() {

        //查询
        User user = userMapper.selectById(2L);
        //修改数据
        user.setNickname("Helen Yao");
        user.setGender((byte)1);
        //执行更新
        userMapper.updateById(user);
    }
    //测试乐观锁插件 失败
    @Test
    public void testOptimisticLockerFail() {
        //查询
        User user = userMapper.selectById(1L);
        //修改数据
        user.setNickname("Helen Yao1");
        user.setGender((byte) 2);
        //模拟取出数据后，数据库中version实际数据比取出的值大，即已被其它线程修改并更新了version
        user.setVersion(user.getVersion() - 1);

        //执行更新
        userMapper.updateById(user);
    }

    //测试分页插件
    @Test
    public void testSelectPage() {

        Page<User> page = new Page<>(1,5);
        userMapper.selectPage(page, null);

        page.getRecords().forEach(System.out::println);
        System.out.println(page.getCurrent());
        System.out.println(page.getPages());
        System.out.println(page.getSize());
        System.out.println(page.getTotal());
        System.out.println(page.hasNext());
        System.out.println(page.hasPrevious());
    }

    //测试 逻辑删除
    @Test
    public void testLogicDelete() {
        int result = userMapper.deleteById(1L);
        System.out.println(result);
    }

    //测试防全表更新与删除插件
    @Test
    public void testFullTable(){
        User user = new User();
        user.setNickname("FULL");
        userService.saveOrUpdate(user, null);
    }


}
