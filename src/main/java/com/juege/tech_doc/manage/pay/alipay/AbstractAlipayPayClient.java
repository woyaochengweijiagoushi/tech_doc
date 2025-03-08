package com.juege.tech_doc.manage.pay.alipay;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alipay.api.AlipayConfig;
import com.alipay.api.AlipayResponse;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.PayOrderDTO;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.juege.tech_doc.dto.pay.PayRespDTO;
import com.juege.tech_doc.dto.pay.PayUnifiedReqDTO;
import com.juege.tech_doc.enums.PayStatusEnum;
import com.juege.tech_doc.manage.pay.AbstractPayClient;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_FORMATTER;


/**
 * 支付宝抽象类，实现支付宝统一的接口
 */
@Getter
@Slf4j
public abstract class AbstractAlipayPayClient extends AbstractPayClient {

	// 仅用于单测场景
	protected DefaultAlipayClient client;

	private final AlipayPayClientConfig config;

	public AbstractAlipayPayClient(String channelCode, AlipayPayClientConfig alipayPayClientConfig) {
		super(channelCode, alipayPayClientConfig);
		this.config = alipayPayClientConfig;
	}

	@Override
	@SneakyThrows
	protected void doInit() {
		AlipayConfig alipayConfig = new AlipayConfig();
		BeanUtil.copyProperties(config, alipayConfig, false);
		this.client = new DefaultAlipayClient(alipayConfig);
	}

	// ============ 支付相关 ==========

	/**
	 * 构造支付关闭的 {@link PayOrderDTO} 对象
	 *
	 * @return 支付关闭的 {@link PayOrderDTO} 对象
	 */
	protected PayRespDTO buildClosedPayOrderRespDTO(PayUnifiedReqDTO reqDTO, AlipayResponse response) {
		Assert.isFalse(response.isSuccess());
		return PayRespDTO.closedOf(response.getSubCode(), response.getSubMsg(),
				reqDTO.getOutTradeNo(), response);
	}

	@Override
	public PayRespDTO doParseOrderNotify(Map<String, String> params, String body) throws Throwable {
		// 1. 校验回调数据
		Map<String, String> bodyObj = HttpUtil.decodeParamMap(body, StandardCharsets.UTF_8);
		if (Objects.equals(config.getMode(), AlipayPayClientConfig.MODE_PUBLIC_KEY)) {
			AlipaySignature.rsaCheckV1(bodyObj, config.getAlipayPublicKey(),
					StandardCharsets.UTF_8.name(), config.getSignType());
		}
		else {
			AlipaySignature.rsaCertCheckV2(bodyObj, config.getAlipayPublicCertContent(),
					StandardCharsets.UTF_8.name(), config.getSignType());
		}


		// 2. 解析订单的状态
		// 额外说明：支付宝不仅仅支付成功会回调，再各种触发支付单数据变化时，都会进行回调，所以这里 status 的解析会写的比较复杂
		Integer status = parseStatus(bodyObj.get("trade_status"));
		// 特殊逻辑: 支付宝没有退款成功的状态，所以，如果有退款金额，我们认为是退款成功
		if (MapUtil.getDouble(bodyObj, "refund_fee", 0D) > 0) {
			status = PayStatusEnum.REFUND.getStatus();
		}
		Assert.notNull(status, (Supplier<Throwable>) () -> {
			throw new IllegalArgumentException(StrUtil.format("body({}) 的 trade_status 不正确", body));
		});
		return PayRespDTO.of(status, bodyObj.get("trade_no"), bodyObj.get("seller_id"), parseTime(params.get("gmt_payment")),
				bodyObj.get("out_trade_no"), body);
	}

	@Override
	protected PayRespDTO doGetOrder(String outTradeNo) throws Throwable {
		// 1.1 构建 AlipayTradeRefundModel 请求
		AlipayTradeQueryModel model = new AlipayTradeQueryModel();
		model.setOutTradeNo(outTradeNo);
		// 1.2 构建 AlipayTradeQueryRequest 请求
		AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
		request.setBizModel(model);
		AlipayTradeQueryResponse response;
		if (Objects.equals(config.getMode(), AlipayPayClientConfig.MODE_CERTIFICATE)) {
			// 证书模式
			response = client.certificateExecute(request);
		}
		else {
			response = client.execute(request);
		}
		if (!response.isSuccess()) { // 不成功，例如说订单不存在
			return PayRespDTO.closedOf(response.getSubCode(), response.getSubMsg(),
					outTradeNo, response);
		}
		// 2.2 解析订单的状态
		Integer status = parseStatus(response.getTradeStatus());
		Assert.notNull(status, () -> {
			throw new IllegalArgumentException(StrUtil.format("body({}) 的 trade_status 不正确", response.getBody()));
		});
		return PayRespDTO.of(status, response.getTradeNo(), response.getBuyerUserId(), LocalDateTimeUtil.of(response.getSendPayDate()),
				outTradeNo, response);
	}

	private static Integer parseStatus(String tradeStatus) {
		return Objects.equals("WAIT_BUYER_PAY", tradeStatus) ? PayStatusEnum.WAITING.getStatus()
				: Set.of("TRADE_FINISHED", "TRADE_SUCCESS").contains(tradeStatus) ? PayStatusEnum.SUCCESS.getStatus()
				: Objects.equals("TRADE_CLOSED", tradeStatus) ? PayStatusEnum.CLOSED.getStatus() : null;
	}

	// ============ 退款相关 ==========


	// ========== 各种工具方法 ==========

	protected String formatAmount(Long amount) {
		return String.valueOf(amount / 100.0);
	}

	protected String formatTime(LocalDateTime time) {
		return LocalDateTimeUtil.format(time, NORM_DATETIME_FORMATTER);
	}

	protected LocalDateTime parseTime(String str) {
		return LocalDateTimeUtil.parse(str, NORM_DATETIME_FORMATTER);
	}

}
