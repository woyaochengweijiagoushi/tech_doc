package com.juege.tech_doc.manage.pay;

import jakarta.validation.Validator;

/**
 * 支付客户端的配置，本质是支付渠道的配置
 * 每个不同的渠道，需要不同的配置，通过子类来定义
 */
public interface PayClientConfig {

	/**
	 * 参数校验
	 *
	 * @param validator 校验对象
	 */
	void validate(Validator validator);


	String getNotifyUrl();


	String getReturnUrl();

}
