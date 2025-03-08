package com.juege.tech_doc.dto.pay;

import java.time.LocalDateTime;

import com.juege.tech_doc.enums.PayOrderDisplayModeEnum;
import com.juege.tech_doc.enums.PayStatusEnum;
import com.juege.tech_doc.exception.BusinessException;
import lombok.Data;

/**
 * 支付订单 Response DTO
 *
 */
@Data
public class PayRespDTO {

	/**
	 * 支付状态
	 *
	 * 枚举：{@link PayStatusEnum}
	 */
	private Integer status;

	/**
	 * 外部订单号
	 *
	 * 对应 PayOrderExtensionDO 的 no 字段
	 */
	private String outTradeNo;

	/**
	 * 支付渠道编号
	 */
	private String channelOrderNo;

	/**
	 * 支付渠道用户编号
	 */
	private String channelUserId;

	/**
	 * 支付成功时间
	 */
	private LocalDateTime successTime;

	/**
	 * 原始的同步/异步通知结果
	 */
	private Object rawData;

	// ========== 主动发起支付时，会返回的字段 ==========

	/**
	 * 展示内容
	 */
	private String displayContent;

	/**
	 * 调用渠道的错误码
	 *
	 * 注意：这里返回的是业务异常，而是不系统异常。
	 * 如果是系统异常，则会抛出 {@link BusinessException}
	 */
	private String channelErrorCode;

	/**
	 * 展示模式
	 *
	 * 枚举 {@link PayOrderDisplayModeEnum} 类
	 */
	private String displayMode;

	/**
	 * 调用渠道报错时，错误信息
	 */
	private String channelErrorMsg;

	public PayRespDTO() {
	}

	/**
	 * 创建【WAITING】状态的订单返回
	 */
	public static PayRespDTO waitingOf(String displayMode, String displayContent,
			String outTradeNo, Object rawData) {
		PayRespDTO respDTO = new PayRespDTO();
		respDTO.status = PayStatusEnum.WAITING.getStatus();
		respDTO.displayMode = displayMode;
		respDTO.displayContent = displayContent;
		// 相对通用的字段
		respDTO.outTradeNo = outTradeNo;
		respDTO.rawData = rawData;
		return respDTO;
	}

	/**
	 * 创建【SUCCESS】状态的订单返回
	 */
	public static PayRespDTO successOf(String channelOrderNo, String channelUserId, LocalDateTime successTime,
			String outTradeNo, Object rawData) {
		PayRespDTO respDTO = new PayRespDTO();
		respDTO.status = PayStatusEnum.SUCCESS.getStatus();
		respDTO.channelOrderNo = channelOrderNo;
		respDTO.channelUserId = channelUserId;
		respDTO.successTime = successTime;
		// 相对通用的字段
		respDTO.outTradeNo = outTradeNo;
		respDTO.rawData = rawData;
		return respDTO;
	}

	/**
	 * 创建指定状态的订单返回，适合支付渠道回调时
	 */
	public static PayRespDTO of(Integer status, String channelOrderNo, String channelUserId, LocalDateTime successTime,
			String outTradeNo, Object rawData) {
		PayRespDTO respDTO = new PayRespDTO();
		respDTO.status = status;
		respDTO.channelOrderNo = channelOrderNo;
		respDTO.channelUserId = channelUserId;
		respDTO.successTime = successTime;
		// 相对通用的字段
		respDTO.outTradeNo = outTradeNo;
		respDTO.rawData = rawData;
		return respDTO;
	}

	/**
	 * 创建【CLOSED】状态的订单返回，适合调用支付渠道失败时
	 */
	public static PayRespDTO closedOf(String channelErrorCode, String channelErrorMsg,
			String outTradeNo, Object rawData) {
		PayRespDTO respDTO = new PayRespDTO();
		respDTO.status = PayStatusEnum.CLOSED.getStatus();
		respDTO.channelErrorCode = channelErrorCode;
		respDTO.channelErrorMsg = channelErrorMsg;
		// 相对通用的字段
		respDTO.outTradeNo = outTradeNo;
		respDTO.rawData = rawData;
		return respDTO;
	}

}
