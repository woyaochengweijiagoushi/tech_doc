package com.juege.tech_doc.req;


import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaySubmitReq {

	@NotNull(message = "金额不能为空")
	@DecimalMin(value = "0.01", message = "金额不能小于0.01")
	private BigDecimal totalAmount;

	private String remark;

	@NotEmpty(message = "订单标题不能为空")
	private String subject;
}
