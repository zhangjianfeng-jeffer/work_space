package com.test;

import java.util.ArrayList;
import java.util.List;

import com.yolo.common.utils.json.JsonTransformUtils;
import com.yolo.common.utils.json.ObjectStringUtils;

public class ObjectStringUtilsTest {

	public static void main(String[] args) throws Exception{
		MyTest a = new MyTest();
		a.setId(1L);
		a.setName("sdsddddddsdsdsdsdsdsdertgrtgsedqwdwdqqwegrtgws	qsqwefwef");
		
		MyTest b = new MyTest();
		b.setId(2L);
		b.setName("cuuuuuuhsdsdsdsdsjslddfdjssjdskdhskdjklsjdlksjldkjklsjdlksjklsucc");
		
		MyTest c = new MyTest();
		c.setId(3L);
		c.setName("cuuuuuuhsdjsldjssjdskdhskdjklsjdlksjldkjklsjdlksjklsucc");
		
		List<MyTest> list = new ArrayList<MyTest>();
		list.add(b);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(b);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		list.add(c);
		a.setUserList(list);
		
		
		ObjectStringUtilsTest.test(a);
		
		long time = System.currentTimeMillis();
		String json = ObjectStringUtils.objectTransformString(a);
		long midd = System.currentTimeMillis();
		System.out.println("tostring time=="+(midd - time));
		System.out.println(json.length()+"=="+json.substring(0, 1000));
		MyTest v=ObjectStringUtils.stringToObject(json);
		String vv = v.toString();
		System.out.println(vv.length()+"==model");
		long end = System.currentTimeMillis();
		System.out.println("to model time=="+(end - midd));
		
		
		
	}
	
	private static void test(Object a)throws Exception{
		System.out.println("--------------------");
		
		long time = System.currentTimeMillis();
		String json = JsonTransformUtils.transformToString(a);
		long midd = System.currentTimeMillis();
		System.out.println("tostring time=="+(midd - time));
		System.out.println(json.length()+"=="+json.substring(0, 1000));
		MyTest d = (MyTest)JsonTransformUtils.transformToObject(json);
		long end = System.currentTimeMillis();
		System.out.println("to model time=="+(end - midd));
		
		
		System.out.println("--------------------");
	}
}
