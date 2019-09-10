package com.yolo.common.utils.json;

import java.io.Serializable;

public class JsonValueObject implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String t;
	private Object v;
	
	public String getT() {
		return t;
	}
	public void setT(String t) {
		this.t = t;
	}
	public Object getV() {
		return v;
	}
	public void setV(Object v) {
		this.v = v;
	}
	@Override
	public String toString() {
		return "JsonValueObject [t=" + t + ", v=" + v + "]";
	}
	
}
