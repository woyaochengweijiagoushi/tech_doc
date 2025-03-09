package com.juege.tech_doc.controller;

import cn.hutool.extra.servlet.JakartaServletUtil;
import com.juege.tech_doc.req.PaySubmitReq;
import com.juege.tech_doc.resp.CommonResp;
import com.juege.tech_doc.resp.PaySubmitResp;
import com.juege.tech_doc.service.PayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay")
public class PayController {

	private final PayService payService;

	public PayController(PayService payService) {
		this.payService = payService;
	}

	@PostMapping("/submit")
	public CommonResp<PaySubmitResp> submit(@Valid @RequestBody PaySubmitReq paySubmitReq, HttpServletRequest httpServletRequest) {
		final String clientIP = JakartaServletUtil.getClientIP(httpServletRequest);
		final PaySubmitResp paySubmitResp = payService.submit(paySubmitReq, clientIP);
		return CommonResp.success(paySubmitResp);
	}

}
