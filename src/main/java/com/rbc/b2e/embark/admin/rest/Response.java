package com.rbc.b2e.embark.admin.rest;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({ "status", "code", "message", "data" })
public class Response implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 901374711416717369L;

	public static class Status {
		public static final String STATUS_SUCCESS = "success";
		public static final String STATUS_FAIL = "fail";
		public static final String STATUS_ERROR = "error";
	}

	private String status;
	private String message;
	private Object data;
	private Integer code;

	public Response() {
		this.status = Status.STATUS_SUCCESS;
		this.code = 200;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String tatus) {
		this.status = tatus;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

}
