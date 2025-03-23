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
 * 文档
 * </p>
 *
 * @author syd
 * @since 2025-03-23
 */
@Getter
@Setter
@ToString
@TableName("doc")
public class Doc implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId("id")
    private Long id;

    /**
     * 电子书id
     */
    @TableField("ebook_id")
    private Long ebookId;

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

    /**
     * 阅读数
     */
    @TableField("view_count")
    private Integer viewCount;

    /**
     * 点赞数
     */
    @TableField("vote_count")
    private Integer voteCount;
}
