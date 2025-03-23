package com.juege.tech_doc.resp;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PaySubmitResp {

	private Integer paymentStatus;

	private String displayMode;

	private String displayContent;

	private String paymentId;

	private String paymentNo;

	private String subject;

	private String remark;

	private LocalDateTime createTime;

	private Long totalAmount;

}
