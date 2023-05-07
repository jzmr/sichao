package com.sichao.service_oss.client;

import com.sichao.common.utils.R;

/**
 * @Description: 熔断器的实现类,要实现Feign接口
 * @author: sjc
 * @createTime: 2023年05月07日 17:35
 */
public class UserDegradeFeignClient implements UserClient{

    //出错之后执行这些方法
    @Override
    public R updateAvatarUrl(String userId, String avatarUrl) {
        return R.error().message("保存头像失败");
    }
}
