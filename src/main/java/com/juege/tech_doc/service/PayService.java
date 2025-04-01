package com.juege.tech_doc.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.juege.tech_doc.domain.TdPay;
import com.juege.tech_doc.domain.TdPayRecord;
import com.juege.tech_doc.domain.User;
import com.juege.tech_doc.dto.pay.PayRespDTO;
import com.juege.tech_doc.dto.pay.PayUnifiedReqDTO;
import com.juege.tech_doc.enums.LongPollStatusEnum;
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
import com.juege.tech_doc.mapper.UserMapper;
import com.juege.tech_doc.req.PageReq;
import com.juege.tech_doc.req.PaySubmitReq;
import com.juege.tech_doc.resp.CommonResp;
import com.juege.tech_doc.resp.PaySubmitResp;
import com.juege.tech_doc.resp.UserLoginResp;
import com.juege.tech_doc.util.JsonUtils;
import com.juege.tech_doc.util.LoginUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayService {

	private final TdPayMapper tdPayMapper;

	private final TdPayRecordMapper tdPayRecordMapper;

	private final PayClientFactory PayClientFactory;

	private final UserMapper userMapper;

	private final PayClientFactory payClientFactory;

	@Transactional(rollbackFor = Exception.class)
	public PaySubmitResp submit(PaySubmitReq paySubmitReq, String userIp) {
		TdPay tdPay = getTdPay(paySubmitReq, userIp);
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
		// 设置同步跳转地址
		payUnifiedReqDTO.setReturnUrl(payClient.getConfig().getReturnUrl().formatted(tdPayRecord.getPaymentNo()));
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
				TdPay newPay = tdPayMapper.selectById(tdPay.getId());
				final Integer paymentStatus = newPay.getPaymentStatus();
				paySubmitResp.setDisplayContent(payRespDTO.getDisplayContent());
				paySubmitResp.setPaymentStatus(paymentStatus);
				paySubmitResp.setDisplayMode(payRespDTO.getDisplayMode());
				paySubmitResp.setPaymentId(String.valueOf(newPay.getId()));
				paySubmitResp.setPaymentNo(newPay.getPaymentNo());
				paySubmitResp.setCreateTime(newPay.getCreatedTime());
				paySubmitResp.setSubject(newPay.getSubject());
				paySubmitResp.setRemark(newPay.getRemark());
				paySubmitResp.setTotalAmount(newPay.getTotalAmount());
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
			notifyPaySuccess(notify);
			return;
		}
		// 情况二：支付失败的回调
		if (PayStatusEnum.isClosed(notify.getStatus())) {
			notifyPayClosed(notify);
		}
		// 情况三：WAITING：无需处理
		// 情况四：REFUND：通过退款回调处理
	}

	private void notifyPaySuccess(PayRespDTO notify) {
		// 1. 更新 TdPayRecord 支付成功
		final TdPayRecord tdPayRecord = updatePaySuccess(notify);
		// 2. 更新 TdPay 支付成功
		final TdPay tdPay = updatePaySuccess(tdPayRecord, notify);

		// 积分追加成功后通过用户查询积分
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				Optional.ofNullable(tdPay).ifPresent(pay -> {
					final Long userId = pay.getUserId();
					LoginUserContext.getLongPollUserMap()
							.getOrDefault(userId, Collections.emptyMap())
							.values().forEach(queue -> {
								queue.forEach(deferredResult -> deferredResult.setResult(CommonResp.success(LongPollStatusEnum.SUCCESS.name())));
							});
				});
			}
		});
	}

	private TdPayRecord updatePaySuccess(PayRespDTO notify) {
		// 1. 查询 TdPayRecord
		final LambdaQueryWrapper<TdPayRecord> tdPayRecordLambdaQueryWrapper = Wrappers.lambdaQuery(TdPayRecord.class).eq(TdPayRecord::getPaymentNo, notify.getOutTradeNo());
		final TdPayRecord tdPayRecord = tdPayRecordMapper.selectOne(tdPayRecordLambdaQueryWrapper);
		Optional.ofNullable(tdPayRecord).orElseThrow(() -> new BusinessException(BusinessExceptionCode.PAY_RECORD_NOT_EXIST_ERROR));
		if (PayStatusEnum.isSuccess(tdPayRecord.getPaymentStatus())) {
			// 如果已经是成功，直接返回，不用重复更新
			log.info("[updatePaySuccess][TdPayRecord({}) 已经是已支付，无需更新]", tdPayRecord.getId());
			return tdPayRecord;
		}
		if (ObjectUtil.notEqual(tdPayRecord.getPaymentStatus(), PayStatusEnum.WAITING.getStatus())) { // 校验状态，必须是待支付
			throw new BusinessException(BusinessExceptionCode.PAY_RECORD_STATUS_IS_NOT_WAITING);
		}

		// 2. 更新 TdPayRecord
		final LambdaUpdateWrapper<TdPayRecord> updateWrapper = Wrappers.lambdaUpdate(TdPayRecord.class)
				.eq(TdPayRecord::getId, tdPayRecord.getId()).eq(TdPayRecord::getPaymentStatus, tdPayRecord.getPaymentStatus())
				.set(TdPayRecord::getPaymentStatus, PayStatusEnum.SUCCESS.getStatus())
				.set(TdPayRecord::getChannelNotifyData, JsonUtils.toJsonString(notify));
		final int updateCounts = tdPayRecordMapper.update(updateWrapper);
		if (updateCounts == 0) { // 校验状态，必须是待支付
			throw new BusinessException(BusinessExceptionCode.PAY_RECORD_STATUS_IS_NOT_WAITING);
		}
		log.info("[updatePaySuccess][TdPayRecord({}) 更新为已支付]", tdPayRecord.getId());
		return tdPayRecord;
	}

	private TdPay updatePaySuccess(TdPayRecord tdPayRecord,
			PayRespDTO notify) {
		// 1. 判断 PayOrderDO 是否处于待支付
		final TdPay tdPay = tdPayMapper.selectById(tdPayRecord.getPaymentId());
		Optional.ofNullable(tdPay).orElseThrow(() -> new BusinessException(BusinessExceptionCode.PAY_NOT_EXIST));
		if (PayStatusEnum.isSuccess(tdPay.getPaymentStatus())) {
			// 如果已经是成功，直接返回，不用重复更新
			log.info("[updatePaySuccess][TdPay({}) 已经是已支付，无需更新]", tdPay.getId());
			return null;
		}
		if (!PayStatusEnum.WAITING.getStatus().equals(tdPay.getPaymentStatus())) { // 校验状态，必须是待支付
			throw new BusinessException(BusinessExceptionCode.PAY_STATUS_IS_NOT_WAITING);
		}

		// 2. 更新 TdPay
		final LambdaUpdateWrapper<TdPay> updateWrapper = Wrappers.lambdaUpdate(TdPay.class)
				.set(TdPay::getPaymentStatus, PayStatusEnum.SUCCESS.getStatus())
				.set(TdPay::getChannelCode, PayChannelEnum.ALIPAY_PC.getCode())
				.set(TdPay::getSuccessTime, notify.getSuccessTime())
				.set(TdPay::getChannelOrderNo, notify.getChannelOrderNo())
				.set(TdPay::getChannelUserId, notify.getChannelUserId())
				.eq(TdPay::getId, tdPay.getId())
				.eq(TdPay::getPaymentStatus, PayStatusEnum.WAITING.getStatus());

		final int updateCounts = tdPayMapper.update(updateWrapper);
		if (updateCounts == 0) {
			// 校验状态，必须是待支付
			throw new BusinessException(BusinessExceptionCode.PAY_STATUS_IS_NOT_WAITING);
		}
		log.info("[updatePaySuccess][TdPay({}) 更新为已支付]", tdPay.getId());

		// 3. 用户追加积分
		final LambdaUpdateWrapper<User> userLambdaUpdateWrapper = Wrappers.lambdaUpdate(User.class)
				.setIncrBy(User::getPoints, tdPay.getTotalAmount())
				.eq(User::getId, tdPay.getUserId());
		userMapper.update(userLambdaUpdateWrapper);
		log.info("[updateUser][User({}) 追加积分{}]", tdPay.getUserId(), tdPay.getTotalAmount());
		return tdPay;
	}

	private void notifyPayClosed(PayRespDTO notify) {
		final TdPayRecord tdPayRecord = updatePayRecordClosed(notify);
		updatePayClosed(tdPayRecord, notify);
	}

	private void updatePayClosed(TdPayRecord tdPayRecord, PayRespDTO notify) {
		// 1. 判断 PayOrderDO 是否处于待支付
		final TdPay tdPay = tdPayMapper.selectById(tdPayRecord.getPaymentId());
		Optional.ofNullable(tdPay).orElseThrow(() -> new BusinessException(BusinessExceptionCode.PAY_NOT_EXIST));
		if (PayStatusEnum.isClosed(tdPay.getPaymentStatus())) {
			// 如果已经是成功，直接返回，不用重复更新
			log.info("[updatePayClosed][TdPay({}) 已经是已关闭，无需更新]", tdPay.getId());
			return;
		}
		if (!PayStatusEnum.WAITING.getStatus().equals(tdPay.getPaymentStatus())) { // 校验状态，必须是待支付
			throw new BusinessException(BusinessExceptionCode.PAY_STATUS_IS_NOT_WAITING);
		}

		// 2. 更新 TdPay
		final LambdaUpdateWrapper<TdPay> updateWrapper = Wrappers.lambdaUpdate(TdPay.class)
				.set(TdPay::getPaymentStatus, PayStatusEnum.CLOSED.getStatus())
				.set(TdPay::getChannelCode, PayChannelEnum.ALIPAY_PC.getCode())
				.set(TdPay::getChannelOrderNo, notify.getChannelOrderNo())
				.set(TdPay::getChannelUserId, notify.getChannelUserId())
				.eq(TdPay::getId, tdPay.getId())
				.eq(TdPay::getPaymentStatus, PayStatusEnum.WAITING.getStatus());
		final int updateCounts = tdPayMapper.update(updateWrapper);
		if (updateCounts == 0) {
			// 校验状态，必须是待支付
			throw new BusinessException(BusinessExceptionCode.PAY_STATUS_IS_NOT_WAITING);
		}
		log.info("[updatePayClosed][TdPay({}) 更新为已关闭]", tdPay.getId());
	}

	private TdPayRecord updatePayRecordClosed(PayRespDTO notify) {
		// 1. 查询 PayRecord
		final LambdaQueryWrapper<TdPayRecord> tdPayRecordLambdaQueryWrapper = Wrappers.lambdaQuery(TdPayRecord.class).eq(TdPayRecord::getPaymentNo, notify.getOutTradeNo());
		final TdPayRecord tdPayRecord = tdPayRecordMapper.selectOne(tdPayRecordLambdaQueryWrapper);
		Optional.ofNullable(tdPayRecord).orElseThrow(() -> new BusinessException(BusinessExceptionCode.PAY_RECORD_NOT_EXIST_ERROR));
		if (PayStatusEnum.isClosed(tdPayRecord.getPaymentStatus())) {
			// 如果已经是成功，直接返回，不用重复更新
			log.info("[updatePayRecordExtensionClosed][TdPayRecord({}) 已经是支付关闭，无需更新]", tdPayRecord.getId());
			return tdPayRecord;
		}

		// 一般出现先是支付成功，然后支付关闭，都是全部退款导致关闭的场景。这个情况，我们不更新支付拓展单，只通过退款流程，更新支付单
		if (PayStatusEnum.isSuccess(tdPayRecord.getPaymentStatus())) {
			log.info("[updatePayRecordExtensionClosed][TdPayRecord({}) 是已支付，无需更新为支付关闭]", tdPayRecord.getId());
			return tdPayRecord;
		}
		if (ObjectUtil.notEqual(tdPayRecord.getPaymentStatus(), PayStatusEnum.WAITING.getStatus())) {
			// 校验状态，必须是待支付
			throw new BusinessException(BusinessExceptionCode.PAY_RECORD_STATUS_IS_NOT_WAITING);
		}

		// 2. 更新 TdPayRecord
		final LambdaUpdateWrapper<TdPayRecord> updateWrapper = Wrappers.lambdaUpdate(TdPayRecord.class)
				.set(TdPayRecord::getPaymentStatus, PayStatusEnum.CLOSED.getStatus())
				.set(TdPayRecord::getChannelNotifyData, JsonUtils.toJsonString(notify))
				.set(TdPayRecord::getChannelErrorCode, notify.getChannelErrorCode())
				.set(TdPayRecord::getChannelErrorMsg, notify.getChannelErrorMsg())
				.eq(TdPayRecord::getId, tdPayRecord.getId())
				.eq(TdPayRecord::getPaymentStatus, tdPayRecord.getPaymentStatus());
		final int updateCounts = tdPayRecordMapper.update(updateWrapper);
		if (updateCounts == 0) { // 校验状态，必须是待支付
			throw new BusinessException(BusinessExceptionCode.PAY_RECORD_STATUS_IS_NOT_WAITING);
		}
		log.info("[updatePayRecordExtensionClosed][TdPayRecord({}) 更新为支付关闭]", tdPayRecord.getId());
		return tdPayRecord;
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

	public TdPay getPay(Long paymentId, Boolean sync, String paymentNo) {
		if (ObjectUtil.isEmpty(paymentId) && StrUtil.isEmpty(paymentNo)) {
			throw new BusinessException(BusinessExceptionCode.PAY_QUERY_PARAMS_NOT_EMPTY);
		}
		final LambdaQueryWrapper<TdPay> queryWrapper = Wrappers.lambdaQuery(TdPay.class)
				.eq(ObjectUtil.isNotEmpty(paymentId), TdPay::getId, paymentId)
				.eq(StrUtil.isNotEmpty(paymentNo), TdPay::getPaymentNo, paymentNo);
		TdPay tdPay = tdPayMapper.selectOne(queryWrapper);

		// 同步查询支付状态
		if (Boolean.TRUE.equals(sync) && PayStatusEnum.isWaiting(tdPay.getPaymentStatus())) {
			syncPayQuietLy(tdPay.getId());
			// 重新查询支付状态
			tdPay = tdPayMapper.selectOne(queryWrapper);
		}
		return tdPay;
	}

	private void syncPayQuietLy(Long paymentId) {
		final LambdaQueryWrapper<TdPayRecord> tdPayRecordLambdaQueryWrapper = Wrappers.lambdaQuery(TdPayRecord.class)
				.eq(TdPayRecord::getPaymentId, paymentId)
				.eq(TdPayRecord::getPaymentStatus, PayStatusEnum.WAITING.getStatus());
		final List<TdPayRecord> tdPayRecords = tdPayRecordMapper.selectList(tdPayRecordLambdaQueryWrapper);
		tdPayRecords.forEach(this::syncPay);
	}


	public boolean syncPay(TdPayRecord tdPayRecord) {
		try {
			// 1.1 查询支付订单信息
			final PayClient<PayClientConfig> payClient = payClientFactory.getPayClient(tdPayRecord.getChannelCode());
			if (payClient == null) {
				log.error("[syncPay][渠道编号({}) 找不到对应的支付客户端]", tdPayRecord.getChannelCode());
				return false;
			}
			final PayRespDTO payRespDTO = payClient.getOrder(tdPayRecord.getPaymentNo());
			// 如果查询到订单不存在，PayClient 返回的状态为关闭。但此时不能关闭订单。存在以下一种场景：
			//  拉起渠道支付后，短时间内用户未及时完成支付，但是该订单同步定时任务恰巧自动触发了，主动查询结果为订单不存在。
			//  当用户支付成功之后，该订单状态在渠道的回调中无法从已关闭改为已支付，造成重大影响。
			// 考虑此定时任务是异常场景的兜底操作，因此这里不做变更，优先以回调为准。
			// 让订单自动随着支付渠道那边一起等到过期，确保渠道先过期关闭支付入口，而后通过订单过期定时任务关闭自己的订单。
			if (PayStatusEnum.isClosed(payRespDTO.getStatus())) {
				return false;
			}
			// 1.2 回调支付结果
			getSelf().notifyOrder(payRespDTO);

			return PayStatusEnum.isSuccess(payRespDTO.getStatus());
		}
		catch (Throwable e) {
			log.error("[syncPay][TdPayRecord({}) 同步支付状态异常]", tdPayRecord.getId(), e);
			return false;
		}
	}


	/**
	 * 同步某个时间之内的订单
	 * @param localDateTime 时间
	 */
	public long syncPay(LocalDateTime localDateTime) {
		final LambdaQueryWrapper<TdPayRecord> queryWrapper = Wrappers.lambdaQuery(TdPayRecord.class)
				.eq(TdPayRecord::getPaymentStatus, PayStatusEnum.WAITING.getStatus())
				.ge(TdPayRecord::getCreatedTime, localDateTime);
		final List<TdPayRecord> tdPayRecords = tdPayRecordMapper.selectList(queryWrapper);
		return tdPayRecords.stream().map(tdPayRecord -> syncPay(tdPayRecord) ? 1L : 0L).reduce(0L, Long::sum);
	}


	public Page<TdPay> listByUser(PageReq pageReq) {
		final UserLoginResp user = LoginUserContext.getUser();
		final Long userId = user.getId();
		final LambdaQueryWrapper<TdPay> queryWrapper = Wrappers.lambdaQuery(TdPay.class)
				.eq(TdPay::getUserId, userId).orderByDesc(TdPay::getId);
		final Page<TdPay> tdPayPage = Page.of(pageReq.getPage(), pageReq.getSize());
		tdPayMapper.selectPage(tdPayPage, queryWrapper);
		return tdPayPage;
	}

}
