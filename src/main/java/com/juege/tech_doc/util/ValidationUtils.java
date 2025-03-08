package com.juege.tech_doc.util;

import java.util.Set;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

/**
 * 校验工具类
 *
 */
public class ValidationUtils {


	public static void validate(Object object, Class<?>... groups) {
		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
		Assert.notNull(validator);
		validate(validator, object, groups);
	}

	public static void validate(Validator validator, Object object, Class<?>... groups) {
		Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object, groups);
		if (CollUtil.isNotEmpty(constraintViolations)) {
			throw new ConstraintViolationException(constraintViolations);
		}
	}

}
