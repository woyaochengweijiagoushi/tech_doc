package com.juege.tech_doc.manage.pay.alipay;

import java.util.Objects;

import com.juege.tech_doc.manage.pay.PayClientConfig;
import com.juege.tech_doc.util.ValidationUtils;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * 支付宝的 PayClientConfig 实现类
 *
 */
@Configuration
@ConfigurationProperties(prefix = "alipay")
@Data
@Validated
public class AlipayPayClientConfig implements PayClientConfig {

	/**
	 * 公钥类型 - 公钥模式
	 */
	public static final Integer MODE_PUBLIC_KEY = 1;

	/**
	 * 公钥类型 - 证书模式
	 */
	public static final Integer MODE_CERTIFICATE = 2;

	/**
	 * 接口内容加密方式 - AES 加密
	 */
	public static final String ENC_TYPE_AES = "AES";

	/**
	 * 签名算法类型 - RSA
	 */
	public static final String SIGN_TYPE_DEFAULT = "RSA2";

	/**
	 * 支付宝回调地址
	 */
	@NotBlank(message = "支付宝回调地址不能为空", groups = { Validate.ModePublicKey.class, Validate.ModeCertificate.class })
	private String notifyUrl;

	/**
	 * 支付宝跳转地址
	 */
	@NotBlank(message = "支付宝跳转地址不能为空", groups = { Validate.ModePublicKey.class, Validate.ModeCertificate.class })
	private String returnUrl;

	/**
	 * 网关地址
	 *
	 * 1. <a href="https://openapi.alipay.com/gateway.do">生产环境</a>
	 * 2. <a href="https://openapi-sandbox.dl.alipaydev.com/gateway.do">沙箱环境</a>
	 */
	@NotBlank(message = "网关地址不能为空", groups = { Validate.ModePublicKey.class, Validate.ModeCertificate.class })
	private String serverUrl;

	/**
	 * 开放平台上创建的应用的 ID
	 */
	@NotBlank(message = "开放平台上创建的应用的 ID不能为空", groups = { Validate.ModePublicKey.class, Validate.ModeCertificate.class })
	private String appId;

	/**
	 * 签名算法类型，推荐：RSA2
	 * <p>
	 * {@link #SIGN_TYPE_DEFAULT}
	 */
	@NotBlank(message = "签名算法类型不能为空", groups = { Validate.ModePublicKey.class, Validate.ModeCertificate.class })
	private String signType;

	/**
	 * 商户私钥
	 */
	@NotBlank(message = "商户私钥不能为空", groups = { Validate.ModePublicKey.class, Validate.ModeCertificate.class })
	private String privateKey;

	/**
	 * 公钥类型
	 * 1. {@link #MODE_PUBLIC_KEY} 情况, alipayPublicKey
	 * 2. {@link #MODE_CERTIFICATE} 情况，appCertContent + alipayPublicCertContent + rootCertContent
	 */
	@NotNull(message = "公钥类型不能为空", groups = { Validate.ModePublicKey.class, Validate.ModeCertificate.class })
	private Integer mode;

	// ========== 公钥模式 ==========

	/**
	 * 支付宝公钥字符串
	 */
	@NotBlank(message = "支付宝公钥字符串不能为空", groups = { Validate.ModePublicKey.class })
	private String alipayPublicKey;

	// ========== 证书模式 ==========

	/**
	 * 指定商户公钥应用证书内容字符串
	 */
	@NotBlank(message = "指定商户公钥应用证书内容不能为空", groups = { Validate.ModeCertificate.class })
	private String appCertContent;

	/**
	 * 指定支付宝公钥证书内容字符串
	 */
	@NotBlank(message = "指定支付宝公钥证书内容不能为空", groups = { Validate.ModeCertificate.class })
	private String alipayPublicCertContent;

	/**
	 * 指定根证书内容字符串
	 */
	@NotBlank(message = "指定根证书内容字符串不能为空", groups = { Validate.ModeCertificate.class })
	private String rootCertContent;


	/**
	 * 接口内容加密方式
	 *
	 * 1. 如果为空，将使用无加密方式
	 * 2. 如果要加密，目前支付宝只有 AES 一种加密方式
	 *
	 * @see <a href="https://opendocs.alipay.com/common/02mse3">支付宝开放平台</a>
	 * @see AlipayPayClientConfig#ENC_TYPE_AES
	 */
	private String encryptType;

	/**
	 * 接口内容加密的私钥
	 */
	private String encryptKey;


	public interface Validate {

		/**
		 * 公钥模式
		 */
		interface ModePublicKey {
		}

		/**
		 * 证书模式
		 */
		interface ModeCertificate {
		}

	}

	@Override
	public void validate(Validator validator) {
		ValidationUtils.validate(validator, this, Objects.equals(this.getMode(), MODE_PUBLIC_KEY) ? Validate.ModePublicKey.class : Validate.ModeCertificate.class);
	}

}
