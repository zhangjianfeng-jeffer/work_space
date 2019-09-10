package com.yolo.common.utils.clas;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.yolo.common.utils.StringUtils;

public class ModleClassManager {
	
	private static Map<String,ModleClassObject> modleClassObjectMap=new ConcurrentHashMap<String, ModleClassObject>();
	
	private synchronized static ModleClassObject createModleClassObject(Class<?> clas){
		String clasStr=clas.toString();
		ModleClassObject modleClassObject=modleClassObjectMap.get(clasStr);
		if(modleClassObject==null){
			modleClassObject=new ModleClassObject(clas);
			modleClassObjectMap.put(clasStr, modleClassObject);
		}
		return modleClassObject;
	}
	
	public static List<Field> getFieldList(Class<?> clas){
		ModleClassObject modleClassObject=modleClassObjectMap.get(clas.toString());
		if(modleClassObject==null){
			modleClassObject=createModleClassObject(clas);
		}
		return modleClassObject.getFieldList();
	}
	
	public static List<Field> getFieldListExceptStatic(Class<?> clas){
		ModleClassObject modleClassObject=modleClassObjectMap.get(clas.toString());
		if(modleClassObject==null){
			modleClassObject=createModleClassObject(clas);
		}
		return modleClassObject.getFieldListExceptStatic();
	}
	
	public static Field getField(Class<?> clas,String fieldName){
		ModleClassObject modleClassObject=modleClassObjectMap.get(clas.toString());
		if(modleClassObject==null){
			modleClassObject=createModleClassObject(clas);
		}
		return modleClassObject.getField(fieldName);
	}
	
	
	public static Method getMethodGet(Class<?> clas,String fieldName){
		Method result=null;
		ModleClassObject modleClassObject=modleClassObjectMap.get(clas.toString());
		if(modleClassObject==null){
			modleClassObject=createModleClassObject(clas);
		}
		if(modleClassObject!=null){
			Field field=modleClassObject.getField(fieldName);
			String methodName = getFieldGetMethodName(field);
			result = modleClassObject.getMethod(methodName);
		}
		
		return result;
	}
	
	
	public static Method getMethodSet(Class<?> clas,String fieldName){
		Method result=null;
		ModleClassObject modleClassObject=modleClassObjectMap.get(clas.toString());
		if(modleClassObject==null){
			modleClassObject=createModleClassObject(clas);
		}
		if(modleClassObject!=null){
			Field field=modleClassObject.getField(fieldName);
			String methodName = getFieldSetMethodName(field);
			result = modleClassObject.getMethod(methodName);
		}
		
		return result;
	}
	
	
	public static Object getObjectValue(Object object,String fieldName)throws Exception{
		Method method=getMethodGet(object.getClass(),fieldName);
		Object obj=method.invoke(object);
		return obj;
	}
	
	public static Object setObjectValue(Object object,String fieldName,Object value)throws Exception{
		Method method=getMethodSet(object.getClass(),fieldName);
		Object obj=method.invoke(object, value);
		return obj;
	}
	
	
	
	
	private static String getFieldGetMethodName(Field field){
		String methodName=null;
		if(field!=null){
			Class<?> type=field.getType();
			String name=field.getName();
			if(type.equals(boolean.class)){
				if(name.indexOf("is")<0){
					methodName="is"+StringUtils.toUpperCaseFirst(name);
				}else{
					methodName=name;
				}
			}else{
				methodName="get"+StringUtils.toUpperCaseFirst(name);
			}
		}
		return methodName;
	}
	
	private static String getFieldSetMethodName(Field field){
		String methodName=null;
		if(field!=null){
			Class<?> type=field.getType();
			String name=field.getName();
			if(type.equals(boolean.class)){
				if(name.indexOf("is")==0){
					methodName="set"+name.replace("is", "");
				}else{
					methodName="set"+StringUtils.toUpperCaseFirst(name);
				}
			}else{
				methodName="set"+StringUtils.toUpperCaseFirst(name);
			}
		}
		return methodName;
	}
	
	
	public static void main(String[] args) throws Exception{
		Test test=new Test();
		
		List<Field> fieldList= ModleClassManager.getFieldList(Test.class);
		
		for (Field field2 : fieldList) {
			String f=field2.getName();
			System.out.println(f);
			if("id".equals(f)){
				ModleClassManager.setObjectValue(test,f ,1000L);
			}else if("name".equals(f)){
				ModleClassManager.setObjectValue(test,f ,"zhang");
			}else if("isState".equals(f)){
				ModleClassManager.setObjectValue(test,f ,true);
			}else if("stateFlag".equals(f)){
				ModleClassManager.setObjectValue(test,f ,true);
			}else if("isOk".equals(f)){
				ModleClassManager.setObjectValue(test,f ,true);
			}else if("okFlag".equals(f)){
				ModleClassManager.setObjectValue(test,f ,true);
			}
		}
		System.out.println(test);
	}
	
}

class Test{
	private Long id;
	
	private String name;
	
	private boolean isState;
	
	private boolean stateFlag;
	
	private Boolean isOk;
	
	private Boolean okFlag;

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

	public boolean isState() {
		return isState;
	}

	public void setState(boolean isState) {
		this.isState = isState;
	}

	public boolean isStateFlag() {
		return stateFlag;
	}

	public void setStateFlag(boolean stateFlag) {
		this.stateFlag = stateFlag;
	}

	public Boolean getIsOk() {
		return isOk;
	}

	public void setIsOk(Boolean isOk) {
		this.isOk = isOk;
	}

	public Boolean getOkFlag() {
		return okFlag;
	}

	public void setOkFlag(Boolean okFlag) {
		this.okFlag = okFlag;
	}

	@Override
	public String toString() {
		return "Test [id=" + id + ", name=" + name + ", isState=" + isState
				+ ", stateFlag=" + stateFlag + ", isOk=" + isOk
				+ ", okFlag=" + okFlag + "]";
	}

	
}
