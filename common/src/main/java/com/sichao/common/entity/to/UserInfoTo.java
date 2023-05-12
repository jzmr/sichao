package com.sichao.common.entity.to;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 用户信息TO类
 * @author: sjc
 * @createTime: 2023年05月01日 11:02
 *
 * TO（Transfer Object）：用于数据传输，通常用于不同系统之间或不同层之间的数据传输。与DTO类似，但更加注重数据传输的效率和性能.
 */
@Data   //提供类的get、set、equals、hashCode、canEqual、toString方法
@NoArgsConstructor // 无参构造器
@AllArgsConstructor //有参构造器
public class UserInfoTo {
    private String userId;//用户id
    private String nickname;//用户昵称
    private String avatarUrl;//用户头像

    public UserInfoTo(String userId, String nickname) {
        this.userId = userId;
        this.nickname = nickname;
    }
}
