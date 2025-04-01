package com.juege.tech_doc.job;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import com.juege.tech_doc.service.PayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayJob {

	private final PayService payService;

	/**
	 * 定时查询支付单状态
	 */
	//@Scheduled(fixedRate = 1L, timeUnit = TimeUnit.MINUTES)
	public void syncPayStatus() {
		log.info("同步支付单开始");
		// 处理5分钟以内支付单
		final LocalDateTime localDateTime = LocalDateTime.now().minusMinutes(5L);
		final long l = payService.syncPay(localDateTime);
		log.info("同步支付单结束,同步了{}个", l);
	}

}
