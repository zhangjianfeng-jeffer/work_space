package com.test;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.yolo.common.utils.json.JsonTransformUtils;


public class JsonTransformUtilsTest {

	public static void main(String[] args) throws Exception{
		MyTest a = new MyTest();
		a.setId(1L);
		a.setName("aaa");
		
		MyTest b = new MyTest();
		b.setId(2L);
		b.setName("bbb");
		
		MyTest c = new MyTest();
		c.setId(3L);
		c.setName("ccc");
		
		List<MyTest> list = new ArrayList<MyTest>();
		list.add(b);
		list.add(c);
		a.setUserList(list);
		
		
		long time = System.currentTimeMillis();
		String json = JsonTransformUtils.transformToString(a);
		System.out.println(System.currentTimeMillis() - time);
		System.out.println(json.length()+"=="+json);
		MyTest d = (MyTest)JsonTransformUtils.transformToObject(json);
		System.out.println(System.currentTimeMillis() - time);
		
	}
	
}

