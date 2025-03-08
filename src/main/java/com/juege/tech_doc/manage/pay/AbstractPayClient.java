package com.juege.tech_doc.manage.pay;

import java.util.Map;

import com.juege.tech_doc.dto.pay.PayRespDTO;
import com.juege.tech_doc.dto.pay.PayUnifiedReqDTO;
import com.juege.tech_doc.exception.BusinessException;
import com.juege.tech_doc.exception.BusinessExceptionCode;
import com.juege.tech_doc.util.JsonUtils;
import com.juege.tech_doc.util.ValidationUtils;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.InitializingBean;


/**
 * 支付客户端的抽象类，提供模板方法，减少子类的冗余代码
 *
 * @author 芋道源码
 */
@Slf4j
public abstract class AbstractPayClient implements PayClient<PayClientConfig>, InitializingBean {

	/**
	 * 渠道编码
	 */
	@SuppressWarnings("FieldCanBeLocal")
	private final String channelCode;

	private final PayClientConfig config;


	public AbstractPayClient(String channelCode, PayClientConfig payClientConfig) {
		this.channelCode = channelCode;
		this.config = payClientConfig;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		init();
	}

	/**
	 * 初始化
	 */
	public final void init() {
		doInit();
		log.debug("[init][客户端({}) 初始化完成]", getChannelCode());
	}

	/**
	 * 自定义初始化
	 */
	protected abstract void doInit();


	@Override
	public String getChannelCode() {
		return channelCode;
	}

	@Override
	public PayClientConfig getConfig() {
		return config;
	}


	// ============ 支付相关 ==========

	@Override
	public final PayRespDTO unifiedOrder(PayUnifiedReqDTO reqDTO) {
		ValidationUtils.validate(reqDTO);
		// 执行统一下单
		PayRespDTO resp;
		try {
			resp = doUnifiedOrder(reqDTO);
		}
		catch (BusinessException ex) { // 业务异常，都是实现类已经翻译，所以直接抛出即可
			throw ex;
		}
		catch (Throwable ex) {
			// 系统异常，则包装成 BusinessException 异常抛出
			log.error("[unifiedOrder][客户端({}) request({}) 发起支付异常]",
					getChannelCode(), JsonUtils.toJsonString(reqDTO), ex);
			throw new BusinessException(BusinessExceptionCode.PAY_CHANNEL_ERROR, ex);
		}
		return resp;
	}

	protected abstract PayRespDTO doUnifiedOrder(PayUnifiedReqDTO reqDTO)
			throws Throwable;

	@Override
	public final PayRespDTO parseOrderNotify(Map<String, String> params, String body) {
		try {
			return doParseOrderNotify(params, body);
		}
		catch (BusinessException ex) { // 业务异常，都是实现类已经翻译，所以直接抛出即可
			throw ex;
		}
		catch (Throwable ex) {
			log.error("[parseOrderNotify][客户端({}) params({}) body({}) 解析失败]",
					getChannelCode(), params, body, ex);
			throw new BusinessException(BusinessExceptionCode.PAY_CHANNEL_ERROR, ex);
		}
	}

	protected abstract PayRespDTO doParseOrderNotify(Map<String, String> params, String body)
			throws Throwable;

	@Override
	public final PayRespDTO getOrder(String outTradeNo) {
		try {
			return doGetOrder(outTradeNo);
		}
		catch (BusinessException ex) { // 业务异常，都是实现类已经翻译，所以直接抛出即可
			throw ex;
		}
		catch (Throwable ex) {
			log.error("[getOrder][客户端({}) outTradeNo({}) 查询支付单异常]",
					getChannelCode(), outTradeNo, ex);
			throw new BusinessException(BusinessExceptionCode.PAY_CHANNEL_ERROR, ex);
		}
	}

	protected abstract PayRespDTO doGetOrder(String outTradeNo)
			throws Throwable;

}
