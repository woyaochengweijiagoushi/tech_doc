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
 * 文档内容
 * </p>
 *
 * @author syd
 * @since 2025-03-08
 */
@Getter
@Setter
@ToString
@TableName("content")
public class Content implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文档id
     */
    @TableId("id")
    private Long id;

    /**
     * 内容
     */
    @TableField("content")
    private String content;
}
