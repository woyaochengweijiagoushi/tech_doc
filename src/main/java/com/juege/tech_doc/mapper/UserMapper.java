package com.juege.tech_doc.mapper;

import com.juege.tech_doc.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户 Mapper 接口
 * </p>
 *
 * @author syd
 * @since 2025-03-23
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
