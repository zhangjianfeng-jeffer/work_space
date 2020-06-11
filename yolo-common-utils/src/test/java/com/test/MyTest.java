package com.test;


import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

public class MyTest {


	private Long id;
	
	private String name;

	private boolean isSuccess;

	private boolean address;

	private Boolean isOk;

	private Boolean result;
	
	private List<MyTest> userList;

	private MyTestEnum myTestEnum;

	private Map<String,MyTest> myTestMap;




	private Object array;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<MyTest> getUserList() {
		return userList;
	}

	public void setUserList(List<MyTest> userList) {
		this.userList = userList;
	}


	@Override
	public String toString() {
		return "MyTest{" +
				"id=" + id +
				", name='" + name + '\'' +
				", isSuccess=" + isSuccess +
				", address=" + address +
				", isOk=" + isOk +
				", result=" + result +
				", userList=" + userList +
				", array=" + array +
				'}';
	}

	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean success) {
		isSuccess = success;
	}

	public Boolean getOk() {
		return isOk;
	}

	public void setOk(Boolean ok) {
		isOk = ok;
	}

	public Boolean getResult() {
		return result;
	}

	public void setResult(Boolean result) {
		this.result = result;
	}

	public boolean isAddress() {
		return address;
	}

	public void setAddress(boolean address) {
		this.address = address;
	}

	public Object getArray() {
		return array;
	}

	public void setArray(Object array) {
		this.array = array;
	}

	public MyTestEnum getMyTestEnum() {
		return myTestEnum;
	}

	public void setMyTestEnum(MyTestEnum myTestEnum) {
		this.myTestEnum = myTestEnum;
	}

	public static void main(String[] args) throws  Exception{
		Constructor<?>[] arr = MyTest.class.getDeclaredConstructors();
		for (int i = 0; i < arr.length; i++) {
			Constructor<?> c = arr[i];
			Class<?>[] cla = c.getParameterTypes();
			if(cla!=null && cla.length>0){
				for (int j = 0; j < cla.length; j++) {
					System.out.println("---"+cla[j]);
				}
			}
		}
	}

	public Map<String, MyTest> getMyTestMap() {
		return myTestMap;
	}

	public void setMyTestMap(Map<String, MyTest> myTestMap) {
		this.myTestMap = myTestMap;
	}
}
