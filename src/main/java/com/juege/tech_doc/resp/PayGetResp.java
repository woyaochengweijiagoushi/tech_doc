package com.juege.tech_doc.resp;

import java.time.LocalDateTime;

import com.juege.tech_doc.domain.TdPay;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

@AutoMapper(target = TdPay.class)
@Data
public class PayGetResp {

	private Integer paymentStatus;

	private String id;

	private String paymentNo;

	private String subject;

	private String remark;

	private LocalDateTime createTime;

	private LocalDateTime successTime;

	private Long userId;

	private String userName;

	private Long totalAmount;

	private String userIp;

}
