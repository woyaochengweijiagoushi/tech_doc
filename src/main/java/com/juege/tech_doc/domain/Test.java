package com.juege.tech_doc.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <p>
 * 测试
 * </p>
 *
 * @author syd
 * @since 2025-03-08
 */
@Getter
@Setter
@ToString
@TableName("test")
public class Test implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId("id")
    private Long id;

    /**
     * 名称
     */
    @TableField("name")
    private String name;

    /**
     * 密码
     */
    @TableField("password")
    private String password;
}
