package com.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yolo.common.utils.json.JsonTransformUtils;
import com.yolo.common.utils.json.ObjectStringUtils;

public class ObjectStringUtilsTest {

	public static void main(String[] args) throws Exception{
		test3();
	}

	private static void test3()throws Exception{
		MyTest a = new MyTest();
		a.setId(1L);
		a.setName("sdsddddddsdsdsdsdsdsdertgrt");
		List<MyTest> list = new ArrayList<MyTest>();
		a.setUserList(list);
		a.setMyTestMap(new HashMap<String, MyTest>());

		MyTest b = new MyTest();
		b.setId(2L);
		b.setName("bbbbbbbbb");
		b.setSuccess(true);
		b.setOk(false);
		b.setMyTestEnum(MyTestEnum.TEST_TWO);
		b.setArray(new MyTest[0]);
		list.add(null);
		list.add(b);
		list.add(null);


		String json = ObjectStringUtils.objectTransformString(a);
		System.out.println("json=="+json);
		MyTest r = ObjectStringUtils.stringTransformObject(json);

		System.out.println(" r=="+ r);

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
		a.setMyTestEnum(MyTestEnum.TEST_ONE);

		MyTest b = new MyTest();
		b.setId(2L);
		b.setName("bbbbbbbbb");
		b.setSuccess(true);
		b.setOk(false);
		b.setMyTestEnum(MyTestEnum.TEST_TWO);

		MyTest c = new MyTest();
		c.setId(3L);
		c.setName("ccccccccc");

		MyTest d = new MyTest();
		d.setId(4L);
		d.setName("ddddddddddddddddd");
		d.setSuccess(false);

		map.put(a,b);
		map.put(c,d);


		MyTest e = new MyTest();
		e.setId(5L);
		e.setName("eeeeeeeeeeeeeee");
		e.setSuccess(false);

		MyTest f = new MyTest();
		f.setId(4L);
		f.setName("ffffffffffff");
		f.setSuccess(false);

		MyTest[] array = new MyTest[2];
		array[0] = e;
		array[1] = f;

		d.setArray(array);




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
