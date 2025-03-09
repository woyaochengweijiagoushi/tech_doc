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
 * 分类
 * </p>
 *
 * @author syd
 * @since 2025-03-08
 */
@Getter
@Setter
@ToString
@TableName("category")
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId("id")
    private Long id;

    /**
     * 父id
     */
    @TableField("parent")
    private Long parent;

    /**
     * 名称
     */
    @TableField("name")
    private String name;

    /**
     * 顺序
     */
    @TableField("sort")
    private Integer sort;
}
