package com.juege.tech_doc;

import java.nio.file.Paths;
import java.sql.Types;
import java.util.Map;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig.Builder;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.engine.BeetlTemplateEngine;
import org.apache.ibatis.annotations.Mapper;

public class MybatisPlusGenerator {


	public static void main(String[] args) {

		String url = "jdbc:mysql://192.168.2.21:3306/tech_doc?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8&autoReconnect=true&rewriteBatchedStatements=true&allowPublicKeyRetrieval=true&nullCatalogMeansCurrent=true";
		String username = "root";
		String password = "ruoyi123";


		final String javaOutputDir = Paths.get(System.getProperty("user.dir")) + "/src/main/java";
		final String resourcesOutputDir = Paths.get(System.getProperty("user.dir")) + "/src/main/resources";
		final Builder dataSourceConfigBuilder = new Builder(url, username, password)
				.typeConvertHandler((globalConfig, typeRegistry, metaInfo) -> {
					if (metaInfo.getJdbcType().TYPE_CODE == Types.TINYINT) {
						return DbColumnType.INTEGER;
					}
					return typeRegistry.getColumnType(metaInfo);
				});
		FastAutoGenerator.create(dataSourceConfigBuilder)
				.globalConfig(builder -> builder
						.author("syd")
						.outputDir(javaOutputDir)
						.commentDate("yyyy-MM-dd")
						.disableOpenDir()
				)
				.packageConfig(builder -> builder
						.parent("com.juege.tech_doc")
						.entity("domain")
						.mapper("mapper")
						.pathInfo(Map.of(OutputFile.xml, resourcesOutputDir + "/mapper"))
				)
				.strategyConfig(builder -> builder
						.entityBuilder()
						.enableLombok()
						.enableFileOverride()
						.enableTableFieldAnnotation()
						.mapperBuilder()
						.mapperAnnotation(Mapper.class)
						.enableFileOverride()
						.serviceBuilder()
						.disableService()
						.disableServiceImpl()
						.controllerBuilder()
						.disable()
				)
				.templateEngine(new BeetlTemplateEngine())
				.execute();
		System.out.println(">>>>>>>>>>>>>>>>>> 生成domain和mapper完成 >>>>>>>>>>>>>>>>");
	}

}
