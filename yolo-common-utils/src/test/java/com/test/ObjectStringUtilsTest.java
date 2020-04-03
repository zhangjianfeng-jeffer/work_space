package com.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yolo.common.utils.json.JsonTransformUtils;
import com.yolo.common.utils.json.ObjectStringUtils;

public class ObjectStringUtilsTest {

	public static void main(String[] args) throws Exception{
		test2();
	}

	private static void test2()throws Exception{
		Map<MyTest,MyTest> map =new HashMap<MyTest, MyTest>();
		MyTest a = new MyTest();
		a.setId(1L);
		a.setName("aaaaaaaaaa");
		a.setSuccess(true);
		a.setOk(true);
		a.setAddress(true);
		a.setResult(true);

		MyTest b = new MyTest();
		b.setId(2L);
		b.setName("bbbbbbbbb");
		b.setSuccess(true);
		b.setOk(false);

		MyTest c = new MyTest();
		c.setId(3L);
		c.setName("ccccccccc");

		MyTest d = new MyTest();
		d.setId(4L);
		d.setName("ddddddddddddddddd");
		d.setSuccess(false);

		map.put(a,b);
		map.put(c,d);

		String json = ObjectStringUtils.objectTransformString(map);
		System.out.println("json======"+json);
		map = ObjectStringUtils.stringTransformObject(json);
		System.out.println("map======"+map);

	}

	private static void testMain()throws Exception{
		MyTest a = new MyTest();
		a.setId(1L);
		a.setName("sdsddddddsdsdsdsdsdsdertgrt");

		MyTest b = new MyTest();
		b.setId(2L);
		b.setName("cuuuuuuhsdsdsdsdsjslddfdj");

		MyTest c = new MyTest();
		c.setId(3L);
		c.setName("cuuuuuuhsdjsldjssjds");

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

		long time1 = System.currentTimeMillis();
		System.out.println("a=="+a.toString());
		System.out.println("a.tostring time=="+(System.currentTimeMillis() - time1));

		ObjectStringUtilsTest.test(a);

		long time = System.currentTimeMillis();
		String json = ObjectStringUtils.objectTransformString(a);
		long midd = System.currentTimeMillis();
		System.out.println("tostring time=="+(midd - time));
		System.out.println(json.length()+"=="+json);
		MyTest v=ObjectStringUtils.stringTransformObject(json);
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
		System.out.println(json.length()+"=="+json);
		MyTest d = (MyTest)JsonTransformUtils.transformToObject(json);
		long end = System.currentTimeMillis();
		System.out.println("to model time=="+(end - midd));
		
		
		System.out.println("--------------------");
	}
}
