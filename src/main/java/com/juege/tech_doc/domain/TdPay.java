package com.juege.tech_doc.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <p>
 * 支付表
 * </p>
 *
 * @author syd
 * @since 2025-03-23
 */
@Getter
@Setter
@ToString
@TableName("td_pay")
public class TdPay implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 支付订单id
     */
    @TableId("id")
    private Long id;

    /**
     * 支付渠道
     */
    @TableField("channel_code")
    private String channelCode;

    /**
     * 支付订单号
     */
    @TableField("payment_no")
    private String paymentNo;

    /**
     * 支付金额 单位分
     */
    @TableField("total_amount")
    private Long totalAmount;

    /**
     * 渠道手续费，单位：百分比
     */
    @TableField("channel_fee_rate")
    private BigDecimal channelFeeRate;

    /**
     * 渠道手续金额，单位：分
     */
    @TableField("channel_fee_price")
    private Long channelFeePrice;

    /**
     * 支付状态（0:待支付, 1:支付成功, 2:已退款, 3:支付关闭）
     */
    @TableField("payment_status")
    private Integer paymentStatus;

    /**
     * 创建时间
     */
    @TableField("created_time")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField("updated_time")
    private LocalDateTime updatedTime;

    /**
     * 用户ip
     */
    @TableField("user_ip")
    private String userIp;

    /**
     * 订单支付成功时间
     */
    @TableField("success_time")
    private LocalDateTime successTime;

    /**
     * 支付备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 订单标题
     */
    @TableField("subject")
    private String subject;

    /**
     * 支付渠道订单号
     */
    @TableField("channel_order_no")
    private String channelOrderNo;

    /**
     * 渠道用户编号
     */
    @TableField("channel_user_id")
    private String channelUserId;
}
