package com.juege.tech_doc.service;

import java.math.BigDecimal;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.juege.tech_doc.domain.TdPay;
import com.juege.tech_doc.domain.TdPayRecord;
import com.juege.tech_doc.dto.pay.PayRespDTO;
import com.juege.tech_doc.dto.pay.PayUnifiedReqDTO;
import com.juege.tech_doc.enums.PayChannelEnum;
import com.juege.tech_doc.enums.PayOrderDisplayModeEnum;
import com.juege.tech_doc.enums.PayStatusEnum;
import com.juege.tech_doc.exception.BusinessException;
import com.juege.tech_doc.exception.BusinessExceptionCode;
import com.juege.tech_doc.manage.pay.PayClient;
import com.juege.tech_doc.manage.pay.PayClientConfig;
import com.juege.tech_doc.manage.pay.PayClientFactory;
import com.juege.tech_doc.mapper.TdPayMapper;
import com.juege.tech_doc.mapper.TdPayRecordMapper;
import com.juege.tech_doc.req.PaySubmitReq;
import com.juege.tech_doc.resp.PaySubmitResp;
import com.juege.tech_doc.resp.UserLoginResp;
import com.juege.tech_doc.util.JsonUtils;
import com.juege.tech_doc.util.LoginUserContext;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
public class PayService {

	private final TdPayMapper tdPayMapper;

	private final TdPayRecordMapper tdPayRecordMapper;

	private final PayClientFactory PayClientFactory;

	public PayService(TdPayMapper tdPayMapper, TdPayRecordMapper tdPayRecordMapper, PayClientFactory PayClientFactory) {
		this.tdPayMapper = tdPayMapper;
		this.tdPayRecordMapper = tdPayRecordMapper;
		this.PayClientFactory = PayClientFactory;
	}

	@Transactional(rollbackFor = Exception.class)
	public PaySubmitResp submit(PaySubmitReq paySubmitReq, String userIp) {
		final TdPay tdPay = getTdPay(paySubmitReq, userIp);
		tdPayMapper.insert(tdPay);
		final PayClient<PayClientConfig> payClient = PayClientFactory.getPayClient(tdPay.getChannelCode());
		final PayUnifiedReqDTO payUnifiedReqDTO = getPayUnifiedReqDTO(userIp, payClient, tdPay);
		final TdPayRecord tdPayRecord = new TdPayRecord();
		tdPayRecord.setId(tdPay.getId());
		tdPayRecord.setPaymentId(tdPay.getId());
		tdPayRecord.setPaymentNo(tdPay.getPaymentNo());
		tdPayRecord.setPaymentStatus(PayStatusEnum.WAITING.getStatus());
		tdPayRecord.setUserIp(userIp);
		tdPayRecord.setChannelCode(tdPay.getChannelCode());
		tdPayRecord.setRequestData(JsonUtils.toJsonString(payUnifiedReqDTO));
		tdPayRecordMapper.insert(tdPayRecord);

		final PaySubmitResp paySubmitResp = new PaySubmitResp();
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void beforeCommit(boolean readOnly) {
				final PayRespDTO payRespDTO = payClient.unifiedOrder(payUnifiedReqDTO);
				if (payRespDTO == null) {
					throw new BusinessException(BusinessExceptionCode.PAY_SUBMIT_CHANNEL_ERROR);
				}
				else {
					// 同步回调
					getSelf().notifyOrder(payRespDTO);
					// 如有渠道错误码，则抛出业务异常，提示用户
					if (StrUtil.isNotEmpty(payRespDTO.getChannelErrorCode())) {
						log.error("{}-- 错误码:{}, 错误信息:{}", BusinessExceptionCode.PAY_SUBMIT_CHANNEL_ERROR.getDesc(), payRespDTO.getChannelErrorCode(), payRespDTO.getChannelErrorMsg());
						throw new BusinessException(BusinessExceptionCode.PAY_SUBMIT_CHANNEL_ERROR);
					}
				}
				// 查询最新的支付状态
				final TdPay newPay = tdPayMapper.selectById(tdPay.getId());
				final Integer paymentStatus = newPay.getPaymentStatus();
				paySubmitResp.setDisplayContent(payRespDTO.getDisplayContent());
				paySubmitResp.setPaymentStatus(paymentStatus);
				paySubmitResp.setDisplayMode(payRespDTO.getDisplayMode());
			}
		});
		return paySubmitResp;
	}

	private static TdPay getTdPay(PaySubmitReq paySubmitReq, String userIp) {
		final UserLoginResp user = LoginUserContext.getUser();
		final TdPay tdPay = new TdPay();
		tdPay.setChannelCode(PayChannelEnum.ALIPAY_PC.getCode());
		tdPay.setRemark(paySubmitReq.getRemark());
		tdPay.setSubject(paySubmitReq.getSubject());
		tdPay.setUserId(user.getId());
		tdPay.setPaymentStatus(PayStatusEnum.WAITING.getStatus());
		tdPay.setUserIp(userIp);
		tdPay.setPaymentNo("P" + IdWorker.getIdStr());
		// 将元转换为分
		final BigDecimal multiply = paySubmitReq.getTotalAmount().multiply(BigDecimal.valueOf(100L));
		tdPay.setTotalAmount(multiply.longValue());
		return tdPay;
	}

	@Transactional(rollbackFor = Exception.class)
	// 注意，如果是方法内调用该方法，需要通过 getSelf().notifyPayOrder(channel, notify) 调用，否则事务不生效
	public void notifyOrder(PayRespDTO notify) {
		// 情况一：支付成功的回调
		if (PayStatusEnum.isSuccess(notify.getStatus())) {
			//notifyOrderSuccess(channel, notify);
			return;
		}
		// 情况二：支付失败的回调
		if (PayStatusEnum.isClosed(notify.getStatus())) {
			//notifyOrderClosed(channel, notify);
		}
		// 情况三：WAITING：无需处理
		// 情况四：REFUND：通过退款回调处理
	}

	private PayUnifiedReqDTO getPayUnifiedReqDTO(String userIp, PayClient<PayClientConfig> payClient, TdPay tdPay) {
		final PayClientConfig payClientConfig = payClient.getConfig();
		final PayUnifiedReqDTO payUnifiedReqDTO = new PayUnifiedReqDTO();
		payUnifiedReqDTO.setBody(tdPay.getRemark());
		payUnifiedReqDTO.setUserIp(userIp);
		payUnifiedReqDTO.setDisplayMode(PayOrderDisplayModeEnum.FORM.getMode());
		payUnifiedReqDTO.setNotifyUrl(payClientConfig.getNotifyUrl());
		payUnifiedReqDTO.setTotalAmount(tdPay.getTotalAmount());
		payUnifiedReqDTO.setSubject(tdPay.getSubject());
		payUnifiedReqDTO.setOutTradeNo(tdPay.getPaymentNo());
		return payUnifiedReqDTO;
	}

	/**
	 * 获得自身的代理对象，解决 AOP 生效问题
	 *
	 * @return 自己
	 */
	private PayService getSelf() {
		return SpringUtil.getBean(getClass());
	}

}
