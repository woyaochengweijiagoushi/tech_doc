package com.juege.tech_doc.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <p>
 * 电子书快照表
 * </p>
 *
 * @author syd
 * @since 2025-03-08
 */
@Getter
@Setter
@ToString
@TableName("ebook_snapshot")
public class EbookSnapshot implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 电子书id
     */
    @TableField("ebook_id")
    private Long ebookId;

    /**
     * 快照日期
     */
    @TableField("date")
    private LocalDate date;

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

    /**
     * 阅读增长
     */
    @TableField("view_increase")
    private Integer viewIncrease;

    /**
     * 点赞增长
     */
    @TableField("vote_increase")
    private Integer voteIncrease;
}
