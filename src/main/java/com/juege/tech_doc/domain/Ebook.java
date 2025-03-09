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
 * 电子书
 * </p>
 *
 * @author syd
 * @since 2025-03-08
 */
@Getter
@Setter
@ToString
@TableName("ebook")
public class Ebook implements Serializable {

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
     * 分类1
     */
    @TableField("category1_id")
    private Long category1Id;

    /**
     * 分类2
     */
    @TableField("category2_id")
    private Long category2Id;

    /**
     * 描述
     */
    @TableField("description")
    private String description;

    /**
     * 封面
     */
    @TableField("cover")
    private String cover;

    /**
     * 文档数
     */
    @TableField("doc_count")
    private Integer docCount;

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
