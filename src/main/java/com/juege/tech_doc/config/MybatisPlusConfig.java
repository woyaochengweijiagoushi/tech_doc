package com.juege.tech_doc.config;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.NetUtils;
import com.baomidou.mybatisplus.core.toolkit.NetUtils.NetProperties;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.juege.tech_doc.util.Snowflake;
import org.mybatis.spring.annotation.MapperScan;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("${mybatis-plus.mapperPackage}")
public class MybatisPlusConfig {


	@Bean
	public MybatisPlusInterceptor mybatisPlusInterceptor() {
		MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
		// 分页插件
		interceptor.addInnerInterceptor(paginationInnerInterceptor());
		return interceptor;
	}

	/**
	 * 分页插件，自动识别数据库类型
	 */
	public PaginationInnerInterceptor paginationInnerInterceptor() {
		PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
		// 分页合理化
		paginationInnerInterceptor.setOverflow(true);
		return paginationInnerInterceptor;
	}

	/**
	 * 配置雪花算法生成器
	 * @return
	 */
	@Bean
	public Snowflake snowflake() {
		final NetUtils netUtils = new NetUtils(new NetProperties());
		return new Snowflake(netUtils.findFirstNonLoopbackAddress());
	}

	/**
	 * 配置mybatis plus 默认id生成器
	 * @param snowflake
	 * @return
	 */
	@Bean
	public IdentifierGenerator identifierGenerator(Snowflake snowflake) {
		IdentifierGenerator identifierGenerator = entity -> snowflake.nextId();
		IdWorker.setIdentifierGenerator(identifierGenerator);
		return identifierGenerator;
	}

}
