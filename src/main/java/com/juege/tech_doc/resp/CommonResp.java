package com.juege.tech_doc.resp;

import lombok.Data;

@Data
public class CommonResp<T> {

	/**
	 * 业务上的成功或失败
	 */
	private boolean success = true;

	/**
	 * 返回信息
	 */
	private String message;

	/**
	 * 返回泛型数据，自定义类型
	 */
	private T content;


	public static <T> CommonResp<T> success(T data) {
		CommonResp<T> result = new CommonResp<>();
		result.success = Boolean.TRUE;
		result.message = "success";
		result.content = data;
		return result;
	}

	public static <T> CommonResp<T> fail(String msg) {
		CommonResp<T> result = new CommonResp<>();
		result.success = Boolean.FALSE;
		result.message = msg == null ? "fail" : msg;
		return result;
	}

}
