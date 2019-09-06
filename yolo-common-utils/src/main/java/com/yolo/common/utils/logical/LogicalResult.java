package com.yolo.common.utils.logical;

/**
 * 逻辑运算器返回结果
 * @author user
 *
 */
public class LogicalResult {

	private boolean success;
	
	private String message;

	
	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
}
