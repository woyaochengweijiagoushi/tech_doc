package com.juege.tech_doc.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * <p>
 * 支付记录表
 * </p>
 *
 * @author syd
 * @since 2025-03-23
 */
@Getter
@Setter
@ToString
@TableName("td_pay_record")
public class TdPayRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 支付记录id
     */
    @TableId("id")
    private Long id;

    /**
     * 支付id
     */
    @TableField("payment_id")
    private Long paymentId;

    /**
     * 用户ip
     */
    @TableField("user_ip")
    private String userIp;

    /**
     * 渠道编码
     */
    @TableField("channel_code")
    private String channelCode;

    @TableField("payment_no")
    private String paymentNo;

    /**
     * 调用渠道的错误码
     */
    @TableField("channel_error_code")
    private String channelErrorCode;

    /**
     * 调用支付渠道时的错误信息
     */
    @TableField("channel_error_msg")
    private String channelErrorMsg;

    /**
     * 请求数据
     */
    @TableField("request_data")
    private String requestData;

    /**
     * 响应数据
     */
    @TableField("response_data")
    private String responseData;

    /**
     * 支付状态（0:待支付, 1:支付成功, 2:已退款, 3:支付关闭）
     */
    @TableField("payment_status")
    private Integer paymentStatus;

    /**
     * 支付渠道的同步/异步通知的内容
     */
    @TableField("channel_notify_data")
    private String channelNotifyData;

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
}
