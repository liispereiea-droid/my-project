package com.labsupplysystem.labbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.labsupplysystem.labbackend.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // MyBatis-Plus 已经帮你写好了 insert, selectById 等方法，这里留空即可
}