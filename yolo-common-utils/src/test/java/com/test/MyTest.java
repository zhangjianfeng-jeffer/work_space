package com.test;


import java.util.List;

public class MyTest {


	private Long id;
	
	private String name;
	
	private List<MyTest> userList;

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
}
