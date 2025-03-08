package com.juege.tech_doc.manage.pay;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.juege.tech_doc.util.ValidationUtils;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * 支付客户端的工厂实现类
 *
 * @author 芋道源码
 */
@Component
@Slf4j
public class PayClientFactoryImpl implements PayClientFactory, InitializingBean {


	private final List<PayClient<PayClientConfig>> payClients;

	@Autowired
	private Validator validator;

	/**
	 * 支付客户端 Map
	 *
	 * key：渠道编号
	 */
	private Map<String, PayClient<PayClientConfig>> clients = Collections.emptyMap();

	public PayClientFactoryImpl(List<PayClient<PayClientConfig>> payClients) {
		this.payClients = payClients;
	}


	@Override
	public PayClient<PayClientConfig> getPayClient(String channelCode) {
		PayClient<PayClientConfig> client = clients.get(channelCode);
		if (client == null) {
			log.error("[getPayClient][渠道编号({}) 找不到客户端]", channelCode);
		}
		return client;
	}

	@Override
	public void afterPropertiesSet() {
		payClients.forEach(payClientConfigPayClient -> ValidationUtils.validate(validator, payClientConfigPayClient.getConfig()));
		clients = payClients.stream().collect(Collectors.toMap(PayClient::getChannelCode, Function.identity()));
	}
}
