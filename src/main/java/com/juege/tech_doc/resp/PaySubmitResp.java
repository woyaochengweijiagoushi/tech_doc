package com.juege.tech_doc.resp;

import lombok.Data;

@Data
public class PaySubmitResp {

	private Integer paymentStatus;

	private String displayMode;

	private String displayContent;

	private String payId;

}
