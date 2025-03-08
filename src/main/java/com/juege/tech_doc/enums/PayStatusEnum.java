package com.juege.tech_doc.enums;

import java.util.Objects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PayStatusEnum {

	WAITING(0, "待支付"),
	SUCCESS(1, "支付成功"),
	REFUND(2, "已退款"),
	CLOSED(3, "支付关闭"),
	;

	private final Integer status;

	private final String desc;

	/**
	 * 判断是否等待支付
	 *
	 * @param status 状态
	 * @return 是否等待支付
	 */
	public static boolean isWaiting(Integer status) {
		return Objects.equals(status, WAITING.getStatus());
	}

	/**
	 * 判断是否支付成功
	 *
	 * @param status 状态
	 * @return 是否支付成功
	 */
	public static boolean isSuccess(Integer status) {
		return Objects.equals(status, SUCCESS.getStatus());
	}

	/**
	 * 判断是否支付关闭
	 *
	 * @param status 状态
	 * @return 是否支付关闭
	 */
	public static boolean isClosed(Integer status) {
		return Objects.equals(status, CLOSED.getStatus());
	}

}
