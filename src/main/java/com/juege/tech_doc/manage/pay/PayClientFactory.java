package com.juege.tech_doc.manage.pay;


/**
 * 支付客户端的工厂接口
 *
 */
public interface PayClientFactory {

	/**
	 * 获得支付客户端
	 *
	 * @param channelCode 渠道编号
	 * @return 支付客户端
	 */
	PayClient<PayClientConfig> getPayClient(String channelCode);

}
