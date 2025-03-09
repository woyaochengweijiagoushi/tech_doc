package com.juege.tech_doc.exception;

public enum BusinessExceptionCode {

	USER_LOGIN_NAME_EXIST("登录名已存在"),
	LOGIN_USER_ERROR("用户名不存在或密码错误"),
	VOTE_REPEAT("您已点赞过"),
	PAY_NOT_EXIST("支付单不存在"),
	PAY_STATUS_SUCCESS("支付已完成"),
	PAY_STATUS_ERROR("支付单状态错误"),
	PAY_NOT_YOURS("不是你的支付单"),
	PAY_CHANNEL_ERROR("支付渠道调用异常"),
	PAY_SUBMIT_CHANNEL_ERROR("发起支付报错!"),
	;

	private String desc;

	BusinessExceptionCode(String desc) {
		this.desc = desc;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
}
