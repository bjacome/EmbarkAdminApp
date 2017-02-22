package com.rbc.b2e.embark.admin.rest;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public class Request<T> {

	private Map<String, Object> theHead;
	private T theData;

	public Map<String, Object> getHead() {
		return theHead;
	}

	public void setHead(Map<String, Object> aHead) {
		theHead = aHead;
	}

	public T getData() {
		return theData;
	}

	public void setData(T aData) {

		// if (theData == null) {
		// // TODO exception class?
		// throw new RuntimeException("MISSING REQUEST DATA");
		// }
		theData = aData;
	}
}
