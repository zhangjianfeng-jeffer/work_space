package com.yolo.common.utils.clas;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class ModleClassObject {
	private Class<?> clas;
	private Map<String,Field> fieldMap;
	private Map<String,Method> methodMap;
	
	
	public ModleClassObject(Class<?> clas){
		this.clas=clas;
	}
	
	private synchronized void initFieldMap(){
		if(fieldMap!=null){
			return;
		}
		fieldMap=ModleClassObject.getClassAllFieldMap(clas);
	}
	
	private synchronized void initMethodMap(){
		if(methodMap!=null){
			return;
		}
		Map<String,Method> methodMapP=new HashMap<String, Method>();
		Method[] methodArray = clas.getMethods();
		if(methodArray!=null&&methodArray.length>0){
			Method method=null;
			String methodName=null;
			for(int i=0;i<methodArray.length;i++){
				method=methodArray[i];
				methodName=method.getName();
				methodMapP.put(methodName, method);
			}
		}
		methodMap=methodMapP;
	}
	
	
	
	
	
	private static Map<String,Field> getClassAllFieldMap(Class<?> clas){
		Map<String,Field> fieldMap=new HashMap<String, Field>();
		Class<?> clasP=clas;
		Field[] fieldArray=null;
		Field field=null;
		while(true){
			if(clasP==null){
				break;
			}
			fieldArray = clasP.getDeclaredFields();
			if(fieldArray!=null&&fieldArray.length>0){
				for(int i=0;i<fieldArray.length;i++){
					field=fieldArray[i];
					field.setAccessible(true);
					if(fieldMap.get(field.getName())==null){
						fieldMap.put(field.getName(), field);
					}
				}
			}
			clasP=clasP.getSuperclass();
		}
		return fieldMap;
	}
	
	
	
	
	
	public List<Field> getFieldList(){
		if(fieldMap==null){
			initFieldMap();
		}
		Iterator<Field> fieldIt=fieldMap.values().iterator();
		List<Field> fieldList=new ArrayList<Field>();
		Field field=null;
		while(fieldIt.hasNext()){
			field=fieldIt.next();
			fieldList.add(field);
		}
		return fieldList;
	}
	
	public List<Field> getFieldListExceptStatic(){
		if(fieldMap==null){
			initFieldMap();
		}
		Iterator<Field> fieldIt=fieldMap.values().iterator();
		List<Field> fieldList=new ArrayList<Field>();
		Field field=null;
		while(fieldIt.hasNext()){
			field=fieldIt.next();
			boolean isStatic = Modifier.isStatic(field.getModifiers());
			if(isStatic==false){
				fieldList.add(field);
			}
		}
		return fieldList;
	}
	
	
	public Field getField(String fieldName){
		if(fieldMap==null){
			initFieldMap();
		}
		return fieldMap.get(fieldName);
	}
	
	
	public Method getMethod(String methodName){
		if(methodMap==null){
			initMethodMap();
		}
		return methodMap.get(methodName);
	}
}
