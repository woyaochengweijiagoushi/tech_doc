package com.juege.tech_doc.manage.pay;

import java.util.Map;

import com.juege.tech_doc.dto.pay.PayRespDTO;
import com.juege.tech_doc.dto.pay.PayUnifiedReqDTO;


/**
 * 支付客户端，用于对接各支付渠道的 SDK，实现发起支付、退款等功能
 *
 */
public interface PayClient<Config extends PayClientConfig> {

	/**
	 * 获得渠道编号
	 *
	 * @return 渠道编号
	 */
	String getChannelCode();

	Config getConfig();

	// ============ 支付相关 ==========

	/**
	 * 调用支付渠道，统一下单
	 *
	 * @param reqDTO 下单信息
	 * @return 支付订单信息
	 */
	PayRespDTO unifiedOrder(PayUnifiedReqDTO reqDTO);

	/**
	 * 解析 order 回调数据
	 *
	 * @param params HTTP 回调接口 content type 为 application/x-www-form-urlencoded 的所有参数
	 * @param body HTTP 回调接口的 request body
	 * @return 支付订单信息
	 */
	PayRespDTO parseOrderNotify(Map<String, String> params, String body);

	/**
	 * 获得支付订单信息
	 *
	 * @param outTradeNo 外部订单号
	 * @return 支付订单信息
	 */
	PayRespDTO getOrder(String outTradeNo);

	// ============ 退款相关 ==========

}
