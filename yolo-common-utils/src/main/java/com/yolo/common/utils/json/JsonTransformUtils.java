package com.yolo.common.utils.json;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yolo.common.utils.DateTimeUtils;
import com.yolo.common.utils.StringUtils;
import com.yolo.common.utils.clas.ModleClassManager;
public class JsonTransformUtils {

	private static Logger logger = LoggerFactory.getLogger(JsonTransformUtils.class);
	
	
	
	
	
	public static Object transformToObject(String jsonStr)throws Exception{
		Object result = null;
		if(StringUtils.isNotBlank(jsonStr)){
			JSONObject jsonOject=new JSONObject(jsonStr);
			JsonValueObject jsonValueObject=convertJsonValueObject(jsonOject);
			result=JsonTransformUtils.convertObject(jsonValueObject);
		}
		return result;
	}
	
	
	public static String transformToString(Object obj)throws Exception{
		JsonValueObject jsonValueObject = JsonTransformUtils.toJsonValueObject(obj);
		JSONObject jsonOject=new JSONObject(jsonValueObject);
		return jsonOject.toString();
	}
	
	
	
	
	public static JsonValueObject toJsonValueObject(Object value)throws Exception{
		JsonValueObject result=null;
		if(value!=null){
			Class<?> clas=value.getClass();
			result=new JsonValueObject();
			String type=clas.getName();
			result.setT(transformTypeToValue(type));
			
			if(type.indexOf("[L")==0){
				Object[] array=(Object[])value;
				List<JsonValueObject> valueList=new ArrayList<JsonValueObject>();
				for (int i = 0; i < array.length; i++) {
					Object obj=array[i];
					if(obj!=null){
						JsonValueObject valueObj=toJsonValueObject(obj);
						if(valueObj!=null){
							valueList.add(valueObj);
						}
					}
				}
				if(valueList!=null && !valueList.isEmpty()){
					result.setV(valueList);
				}
			}else if(value instanceof List){
				List<?> list=(List<?>)value;
				List<JsonValueObject> valueList=new ArrayList<JsonValueObject>();
				for (Object obj : list) {
					JsonValueObject valueObj=toJsonValueObject(obj);
					if(valueObj!=null){
						valueList.add(valueObj);
					}
				}
				if(valueList!=null && !valueList.isEmpty()){
					result.setV(valueList);
				}
			}else if(value instanceof Map){
				Map<?,?> map=(Map<?,?>)value;
				Map<String,JsonValueObject> valueMap=new HashMap<String, JsonValueObject>();
				Iterator<?> it=map.keySet().iterator();
				while (it.hasNext()) {
					Object key=it.next();
					Object valueObject=map.get(key);
					JsonValueObject jObjK=toJsonValueObject(key);
					JsonValueObject jObjV=toJsonValueObject(valueObject);
					
					if(jObjK!=null && jObjV!=null){
						JSONObject jsonOjectKey=new JSONObject(jObjK);
						valueMap.put(jsonOjectKey.toString(), jObjV);
					}
				}
				if(valueMap!=null && !valueMap.isEmpty()){
					result.setV(valueMap);
				}
			}else{
				if(isBaseType(value.getClass().getName())){
					Object val=getBaseTypeValue(value);
					if(val!=null){
						result.setV(val);
					}
				}else{
					Map<String,JsonValueObject> valueMap=null;
					List<Field> fieldList=ModleClassManager.getFieldListExceptStatic(clas);
					if(fieldList!=null && !fieldList.isEmpty()){
						valueMap=new HashMap<String, JsonValueObject>();
						for (Field field : fieldList) {
							Object fieldValue = field.get(value);
							if(fieldValue!=null){
								String fieldName=field.getName();
								Method methodGet = ModleClassManager.getMethodGet(clas, fieldName);
								if(methodGet!=null && Modifier.isPublic(methodGet.getModifiers())){
									JsonValueObject jObjV=toJsonValueObject(fieldValue);
									if(jObjV!=null){
										valueMap.put(fieldName ,jObjV);
									}
								}
							}
						}
					}
					if(valueMap!=null && !valueMap.isEmpty()){
						result.setV(valueMap);
					}
				}
			}
		}
		return result;
	}
	
	
	public static Object convertObject(JsonValueObject jsonValueObject)throws Exception{
		Object result=null;
		if(jsonValueObject!=null){
			String type=jsonValueObject.getT();
			Object value=jsonValueObject.getV();
			if(StringUtils.isNotBlank(type) && value != null){
				if(isBaseType(type)){
					result = value;
				}else{
					Class<?> clas=Class.forName(type);
					
					if(type.indexOf("[L")==0){
						@SuppressWarnings("unchecked")
						List<JsonValueObject> valueList=(List<JsonValueObject>)value;
						if(valueList!=null && !valueList.isEmpty()){
							String typeItem=type.replace("[L", "");
							typeItem=typeItem.replace(";", "");
							clas=Class.forName(typeItem);
							Object array = Array.newInstance(clas, valueList.size());
							for (int i = 0; i < valueList.size(); i++) {
								Array.set(array, i, convertObject(valueList.get(i)));
							}
							result=array;
						}
					}else{
						Object object = clas.newInstance();
						if(object instanceof List){
							@SuppressWarnings("unchecked")
							List<JsonValueObject> valueList=(List<JsonValueObject>)value;
							@SuppressWarnings("unchecked")
							List<Object> list=(List<Object>)object;
							if(valueList!=null){
								for (JsonValueObject jsonVal : valueList) {
									list.add(convertObject(jsonVal));
								}
							}
							result=list;
						}else if(object instanceof Map){
							@SuppressWarnings("unchecked")
							Map<JsonValueObject,JsonValueObject> map=(Map<JsonValueObject,JsonValueObject>)value;
							@SuppressWarnings("unchecked")
							Map<Object,Object> valueMap=(Map<Object,Object>)object;
							if(map!=null){
								Iterator<JsonValueObject> it=map.keySet().iterator();
								while(it.hasNext()){
									JsonValueObject key=it.next();
									JsonValueObject valueJson=map.get(key);
									valueMap.put(convertObject(key), convertObject(valueJson));
								}
							}
							result=valueMap;
						}else{
							@SuppressWarnings("unchecked")
							Map<String,JsonValueObject> valueMap=(Map<String,JsonValueObject>)value;
							List<Field> fieldList=ModleClassManager.getFieldListExceptStatic(clas);
							if(fieldList!=null && !fieldList.isEmpty()){
								for (Field field : fieldList) {
									String fieldName=field.getName();
									JsonValueObject fieldValue = valueMap.get(fieldName);
									if(fieldValue!=null){
										Method methodSet = ModleClassManager.getMethodSet(clas, fieldName);
										if(methodSet!=null && Modifier.isPublic(methodSet.getModifiers())){
											try {
												field.set(object, convertObject(fieldValue));
											} catch (Exception e) {
												logger.error("fieldName:"+fieldName+",fieldValue:"+fieldValue+",methodSet");
												e.printStackTrace();
												throw e;
											}
											
										}
									}
								}
							}
							result=object;
						}
					
					}
				}
			}
		}
		return result;
	}
	
	
	public static JsonValueObject convertJsonValueObject(JSONObject jsonOject)throws Exception{
		JsonValueObject result=null;
		if(jsonOject!=null){
			if(jsonOject.has("t") && jsonOject.has("v")){
				String type = jsonOject.getString("t");
				type=transformValueToType(type);
				if(StringUtils.isNotBlank(type)){
					result = new JsonValueObject();
					result.setT(type);
					
					if(isBaseType(type)){
						Object value = jsonOject.get("v");
						if(value!=null){
							Object valueObj=getValueByType(value.toString(),type);
							result.setV(valueObj);
						}
					}else{
						Class<?> clas=Class.forName(type);
						if(type.indexOf("[L")==0){
							JSONArray value = jsonOject.getJSONArray("v");
							List<JsonValueObject> list=null;
							if(value!=null){
								list = new ArrayList<JsonValueObject>();
								for (int i = 0; i < value.length(); i++) {
									JSONObject json=value.getJSONObject(i);
									JsonValueObject jsonValue=convertJsonValueObject(json);
									if(jsonValue!=null){
										list.add(jsonValue);
									}
								}
							}
							result.setV(list);
						}else{
							Object object = clas.newInstance();
							if(object instanceof List){
								JSONArray value = jsonOject.getJSONArray("v");
								List<JsonValueObject> list=null;
								if(value!=null){
									list = new ArrayList<JsonValueObject>();
									for (int i = 0; i < value.length(); i++) {
										JSONObject json=value.getJSONObject(i);
										JsonValueObject jsonValue=convertJsonValueObject(json);
										if(jsonValue!=null){
											list.add(jsonValue);
										}
									}
								}
								result.setV(list);
							}else if(object instanceof Map){
								JSONObject value = jsonOject.getJSONObject("v");
								if(value!=null){
									Map<JsonValueObject,JsonValueObject> map=new HashMap<JsonValueObject, JsonValueObject>();
									@SuppressWarnings("unchecked")
									Iterator<String> it=value.keySet().iterator();
									while(it.hasNext()){
										String jsonKey=it.next();
										JSONObject jsonKeyVal=value.getJSONObject(jsonKey);
										
										JSONObject jsonOjectKey=new JSONObject(jsonKey);
										JsonValueObject jvk=convertJsonValueObject(jsonOjectKey);
										JsonValueObject jvv=convertJsonValueObject(jsonKeyVal);
										if(jvk!=null && jvv!=null){
											map.put(jvk, jvv);
										}
									}
									if(map!=null && !map.isEmpty()){
										result.setV(map);
									}
								}
							}else{
								JSONObject value = jsonOject.getJSONObject("v");
								if(value!=null){
									Map<String,JsonValueObject> map=new HashMap<String, JsonValueObject>();
									@SuppressWarnings("unchecked")
									Iterator<String> it=value.keySet().iterator();
									while(it.hasNext()){
										String feildName=it.next();
										JSONObject feildVal=value.getJSONObject(feildName);
										JsonValueObject jvv=convertJsonValueObject(feildVal);
										if(jvv!=null){
											map.put(feildName, jvv);
										}
									}
									if(map!=null && !map.isEmpty()){
										result.setV(map);
									}
								}
							}
						}
					}
				}
			}
		}
		return result;
	}
	
	
	
	private static boolean isBaseType(String clas){
		boolean isBaseType=false;
		if(clas.equals(byte.class.getName())||clas.equals(Byte.class.getName())){
			isBaseType=true;
		}else if(clas.equals(boolean.class.getName())||clas.equals(Boolean.class.getName())){
			isBaseType=true;
		}else if(clas.equals(int.class.getName())||clas.equals(Integer.class.getName())){
			isBaseType=true;
		}else if(clas.equals(long.class.getName())||clas.equals(Long.class.getName())){
			isBaseType=true;
		}else if(clas.equals(float.class.getName())||clas.equals(Float.class.getName())){
			isBaseType=true;
		}else if(clas.equals(double.class.getName())||clas.equals(Double.class.getName())){
			isBaseType=true;
		}else if(clas.equals(char.class.getName())){
			isBaseType=true;
		}else if(clas.equals(String.class.getName())){
			isBaseType=true;
		}else if(clas.equals(Date.class.getName())){
			isBaseType=true;
		}else if(clas.equals(BigDecimal.class.getName())){
			isBaseType=true;
		}
		return isBaseType;
	}
	
	
	private static Object getBaseTypeValue(Object value)throws Exception{
		Object result=null;
		if(value!=null){
			Class<?> clas=value.getClass();
			if(clas.equals(byte.class)||clas.equals(Byte.class)){
				result=value;
			}else if(clas.equals(boolean.class)||clas.equals(Boolean.class)){
				result=value;
			}else if(clas.equals(int.class)||clas.equals(Integer.class)){
				result=value;
			}else if(clas.equals(long.class)||clas.equals(Long.class)){
				result=value;
			}else if(clas.equals(float.class)||clas.equals(Float.class)){
				result=value;
			}else if(clas.equals(double.class)||clas.equals(Double.class)){
				result=value;
			}else if(clas.equals(char.class)){
				char[] charArray=value.toString().toCharArray();
				if(charArray!=null && charArray.length>0){
					result=charArray[0];
				}
			}else if(clas.equals(String.class)){
				result=value;
			}else if(clas.equals(Date.class)){
				result=DateTimeUtils.dateFormatString((Date)value, DateTimeUtils.FORMAT_Y_M_D_H_M_S_S_1);
			}else if(clas.equals(BigDecimal.class)){
				result=value.toString();
			}
		}
		return result;
	}
	
	private static Object getValueByType(String value,String type)throws Exception{
		Object result=null;
		if(StringUtils.isNotBlank(value)){
			if(type.equals(byte.class.getName())||type.equals(Byte.class.getName())){
				result=Byte.parseByte(value);
			}else if(type.equals(boolean.class.getName())||type.equals(Boolean.class.getName())){
				result=Boolean.getBoolean(value);
			}else if(type.equals(int.class.getName())||type.equals(Integer.class.getName())){
				result=Integer.parseInt(value);
			}else if(type.equals(long.class.getName())||type.equals(Long.class.getName())){
				result=Long.parseLong(value);
			}else if(type.equals(float.class.getName())||type.equals(Float.class.getName())){
				result=Float.parseFloat(value);
			}else if(type.equals(double.class.getName())||type.equals(Double.class.getName())){
				result=Double.parseDouble(value);
			}else if(type.equals(char.class.getName())){
				char[] charArray=value.toString().toCharArray();
				if(charArray!=null && charArray.length>0){
					result=charArray[0];
				}
			}else if(type.equals(String.class.getName())){
				result=value;
			}else if(type.equals(Date.class.getName())){
				result=DateTimeUtils.stringFormatDate(value, DateTimeUtils.FORMAT_Y_M_D_H_M_S_S_1);
			}else if(type.equals(BigDecimal.class.getName())){
				result=new BigDecimal(value);
			}
		}
		return result;
	}
	
	
	private static Map<String,String> mapTypeValue;
	private static Map<String,String> mapValueType;
	
	private synchronized static void initMap(){
		if(mapTypeValue==null){
			mapTypeValue=new HashMap<String, String>();
			mapTypeValue.put(byte.class.getName(), Integer.toString(1));
			mapTypeValue.put(Byte.class.getName(), Integer.toString(2));
			mapTypeValue.put(boolean.class.getName(), Integer.toString(3));
			mapTypeValue.put(Boolean.class.getName(), Integer.toString(4));
			mapTypeValue.put(int.class.getName(), Integer.toString(5));
			mapTypeValue.put(Integer.class.getName(), Integer.toString(6));
			mapTypeValue.put(long.class.getName(), Integer.toString(7));
			mapTypeValue.put(Long.class.getName(), Integer.toString(8));
			mapTypeValue.put(float.class.getName(), Integer.toString(9));
			mapTypeValue.put(Float.class.getName(), Integer.toString(10));
			mapTypeValue.put(double.class.getName(), Integer.toString(11));
			mapTypeValue.put(Double.class.getName(), Integer.toString(12));
			mapTypeValue.put(char.class.getName(), Integer.toString(13));
			mapTypeValue.put(String.class.getName(), Integer.toString(14));
			mapTypeValue.put(char.class.getName(), Integer.toString(15));
			mapTypeValue.put(Date.class.getName(), Integer.toString(16));
			mapTypeValue.put(BigDecimal.class.getName(), Integer.toString(17));
			mapTypeValue.put(ArrayList.class.getName(), Integer.toString(18));
			mapTypeValue.put(HashMap.class.getName(), Integer.toString(19));
		}
		if(mapValueType==null){
			mapValueType=new HashMap<String, String>();
			Iterator<String> it=mapTypeValue.keySet().iterator();
			while(it.hasNext()){
				String key=it.next();
				String value=mapTypeValue.get(key);
				mapValueType.put(value, key);
			}
		}
	}
	
	private static String transformTypeToValue(String type){
		if(mapTypeValue==null || mapValueType==null){
			initMap();
		}
		String value=mapTypeValue.get(type);
		if(StringUtils.isBlank(value)){
			value=type;
		}
		return value;
	}
	
	private static String transformValueToType(String value){
		if(mapTypeValue==null || mapValueType==null){
			initMap();
		}
		String type=mapValueType.get(value);
		if(StringUtils.isBlank(type)){
			type=value;
		}
		return type;
	}
	
}



