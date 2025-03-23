package com.juege.tech_doc.controller;

import java.util.Map;

import cn.hutool.extra.servlet.JakartaServletUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.juege.tech_doc.domain.TdPay;
import com.juege.tech_doc.dto.pay.PayRespDTO;
import com.juege.tech_doc.enums.PayChannelEnum;
import com.juege.tech_doc.manage.pay.PayClient;
import com.juege.tech_doc.manage.pay.PayClientConfig;
import com.juege.tech_doc.manage.pay.PayClientFactory;
import com.juege.tech_doc.req.PageReq;
import com.juege.tech_doc.req.PaySubmitReq;
import com.juege.tech_doc.resp.CommonResp;
import com.juege.tech_doc.resp.PayGetResp;
import com.juege.tech_doc.resp.PaySubmitResp;
import com.juege.tech_doc.service.PayService;
import io.github.linpeilie.Converter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/pay")
public class PayController {

	private final PayService payService;

	private final PayClientFactory payClientFactory;

	private final Converter converter;

	public PayController(PayService payService, PayClientFactory payClientFactory, Converter converter) {
		this.payService = payService;
		this.payClientFactory = payClientFactory;
		this.converter = converter;
	}

	@PostMapping("/submit")
	public CommonResp<PaySubmitResp> submit(@Valid @RequestBody PaySubmitReq paySubmitReq, HttpServletRequest httpServletRequest) {
		final String clientIP = JakartaServletUtil.getClientIP(httpServletRequest);
		final PaySubmitResp paySubmitResp = payService.submit(paySubmitReq, clientIP);
		return CommonResp.success(paySubmitResp);
	}

	@PostMapping("/notify/alipay")
	public String notify(@RequestParam(required = false) Map<String, String> params,
			@RequestBody(required = false) String body) {
		log.info("[notify][alipay 回调数据({}/{})]", params, body);
		final PayClient<PayClientConfig> payClient = payClientFactory.getPayClient(PayChannelEnum.ALIPAY_PC.getCode());
		final PayRespDTO payRespDTO = payClient.parseOrderNotify(params, body);
		payService.notifyOrder(payRespDTO);
		return "success";
	}

	@GetMapping("/get")
	public CommonResp<PayGetResp> get(@RequestParam(value = "paymentId", required = false) Long paymentId, @RequestParam(value = "paymentNo", required = false) String paymentNo,
			@RequestParam(value = "sync", required = false, defaultValue = "false") Boolean sync) {
		final TdPay pay = payService.getPay(paymentId, sync, paymentNo);
		final PayGetResp payGetResp = converter.convert(pay, PayGetResp.class);
		return CommonResp.success(payGetResp);
	}

	@GetMapping("/list-by-user")
	public CommonResp<Page<TdPay>> listByUser(PageReq pageReq) {
		final Page<TdPay> tdPayPage = payService.listByUser(pageReq);
		return CommonResp.success(tdPayPage);
	}

}
