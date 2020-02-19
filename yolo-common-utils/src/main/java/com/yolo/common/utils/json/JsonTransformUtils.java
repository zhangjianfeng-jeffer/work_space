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
			result = convertObjectMain(jsonOject);		
		}
		return result;
	}
	
	
	public static String transformToString(Object obj)throws Exception{
		String result = null;
		JSONObject jsonOject = JsonTransformUtils.toJsonValueObjectMain(obj);
		if(jsonOject!=null){
			result = jsonOject.toString();
		}
		return result;
	}
	
	public static Object convertObjectMain(JSONObject jsonOject)throws Exception{
		Object result = null;
		if(jsonOject!=null){
			JSONObject objectValue = jsonOject.getJSONObject("objectValue");
			JSONObject typeValueMap = jsonOject.getJSONObject("typeValueMap");
			if(objectValue!=null && typeValueMap !=null){
				TypeValue typeValue = new TypeValue();
				JsonValueObject typeValueMapJsonValue=convertJsonValueObject(typeValueMap,typeValue);
				Map<String,String> typeValueMapNew = getTypeValueMap(typeValueMapJsonValue);
				typeValue.addTypeValueMap(typeValueMapNew);
				JsonValueObject objectValueJsonValue=convertJsonValueObject(objectValue,typeValue);
				result=JsonTransformUtils.convertObject(objectValueJsonValue);
			}
		}
		return result;
	}
	
	private static Map<String,String> getTypeValueMap(JsonValueObject typeValueMap){
		Map<String,String> result = null;
		if(typeValueMap!=null){
			@SuppressWarnings("unchecked")
			Map<JsonValueObject,JsonValueObject> map=(Map<JsonValueObject,JsonValueObject>)typeValueMap.getV();
			if(map!=null){
				result = new HashMap<String, String>();
				Iterator<JsonValueObject> it = map.keySet().iterator();
				while(it.hasNext()){
					JsonValueObject key = it.next();
					JsonValueObject value = map.get(key);
					String keyValue = getJsonValueObjectValue(key);
					String valueValue = getJsonValueObjectValue(value);
					result.put(keyValue,valueValue);
				}
			}
		}
		return result;
	}
	
	private static String getJsonValueObjectValue(JsonValueObject bean){
		String value = null;
		if(bean!=null){
			Object obj = bean.getV();
			if(obj!=null){
				value = obj.toString();
			}
		}
		return value;
	}
	
	
	public static JSONObject toJsonValueObjectMain(Object value)throws Exception{
		TypeValue typeValue = new TypeValue();
		JsonValueObject jsonValueObject = JsonTransformUtils.toJsonValueObject(value,typeValue);
		Map<String,String> map = typeValue.getTypeValueMap();
		JsonValueObject typeMapJSON = toJsonValueObject(map,new TypeValue());
		JSONObject jsonValueObjectJson = new JSONObject(jsonValueObject);
		JSONObject typeMapJSONJson = new JSONObject(typeMapJSON);
		JSONObject result = new JSONObject();
		result.put("objectValue", jsonValueObjectJson);
		result.put("typeValueMap", typeMapJSONJson);
		return result;
	}
	
	
	public static JsonValueObject toJsonValueObject(Object value,TypeValue typeValue)throws Exception{
		JsonValueObject result=null;
		if(value!=null && typeValue!=null){
			Class<?> clas=value.getClass();
			String type=clas.getName();
			result=new JsonValueObject();
			result.setT(transformTypeToValue(type,typeValue));
			
			if(type.indexOf("[L")==0){
				Object[] array=(Object[])value;
				List<JsonValueObject> valueList=new ArrayList<JsonValueObject>();
				for (int i = 0; i < array.length; i++) {
					Object obj=array[i];
					if(obj!=null){
						JsonValueObject valueObj=toJsonValueObject(obj,typeValue);
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
					JsonValueObject valueObj=toJsonValueObject(obj,typeValue);
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
					JsonValueObject jObjK=toJsonValueObject(key,typeValue);
					JsonValueObject jObjV=toJsonValueObject(valueObject,typeValue);
					
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
									JsonValueObject jObjV=toJsonValueObject(fieldValue,typeValue);
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
	
	
	public static JsonValueObject convertJsonValueObject(JSONObject jsonOject,TypeValue typeValue)throws Exception{
		JsonValueObject result=null;
		if(jsonOject!=null){
			if(jsonOject.has("t") && jsonOject.has("v")){
				String type = jsonOject.getString("t");
				type=transformValueToType(type,typeValue);
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
									JsonValueObject jsonValue=convertJsonValueObject(json,typeValue);
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
										JsonValueObject jsonValue=convertJsonValueObject(json,typeValue);
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
										JsonValueObject jvk=convertJsonValueObject(jsonOjectKey,typeValue);
										JsonValueObject jvv=convertJsonValueObject(jsonKeyVal,typeValue);
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
										JsonValueObject jvv=convertJsonValueObject(feildVal,typeValue);
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
				result=DateTimeUtils.dateFormatString((Date)value, DateTimeUtils.FormatType.FORMAT_Y_M_D_H_M_S_S_1);
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
				result=DateTimeUtils.stringFormatDate(value, DateTimeUtils.FormatType.FORMAT_Y_M_D_H_M_S_S_1);
			}else if(type.equals(BigDecimal.class.getName())){
				result=new BigDecimal(value);
			}
		}
		return result;
	}
	
	
	
	
	
	private static String transformTypeToValue(String type,TypeValue typeValue)throws Exception{
		typeValue.setType(type);
		String value = typeValue.getValueByType(type);
		if(StringUtils.isBlank(value)){
			throw new Exception("类型对应的值为空");
		}
		return value;
	}
	
	private static String transformValueToType(String value,TypeValue typeValue)throws Exception{
		String type=typeValue.getTypeByValue(value);
		if(StringUtils.isBlank(type)){
			throw new Exception("对应的值为空");
		}
		return type;
	}
	
}

class TypeValue{
	private int index;
	private Map<String,String> typeValueMap = new HashMap<String, String>();
	private Map<String,String> valueTypeMap = new HashMap<String, String>();
	
	public TypeValue(){
		this.index = typeValueDefultMap.size()+1;
	}
	
	
	public void addTypeValueMap(Map<String,String> map){
		if(map !=null && !map.isEmpty()){
			Iterator<String> it = map.keySet().iterator();
			while(it.hasNext()){
				String key = it.next();
				String value = map.get(key);
				if(StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)){
					typeValueMap.put(key, value);
					valueTypeMap.put(value,key);
				}
			}
		}
	}
	
	
	public void setType(String type){
		if(StringUtils.isNotBlank(type)){
			if(TypeValue.getDefultValueByType(type)==null){
				if(typeValueMap.get(type) == null){
					String value = Integer.toString(index++);
					typeValueMap.put(type, value);
					valueTypeMap.put(value, type);
				}
			}
		}
	}
	
	public String getValueByType(String type){
		String value = TypeValue.getDefultValueByType(type);
		if(value==null){
			value = typeValueMap.get(type);
		}
		return value;
	}
	public String getTypeByValue(String value){
		String type = TypeValue.getDefultTypeByValue(value);
		if(type == null){
			type = valueTypeMap.get(value);
		}
		return type;
	}
	
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public Map<String, String> getTypeValueMap() {
		return typeValueMap;
	}

	public void setTypeValueMap(Map<String, String> typeValueMap) {
		this.typeValueMap = typeValueMap;
	}
	
	
	private static String getDefultValueByType(String type){
		return typeValueDefultMap.get(type);
	}
	
	private static String getDefultTypeByValue(String value){
		return valueTypeDefultMap.get(value);
	}
	
	public static Map<String,String> typeValueDefultMap = new HashMap<String, String>();
	public static Map<String,String> valueTypeDefultMap = new HashMap<String, String>();
	static{
		typeValueDefultMap.put(byte.class.getName(), Integer.toString(1));
		typeValueDefultMap.put(Byte.class.getName(), Integer.toString(2));
		typeValueDefultMap.put(boolean.class.getName(), Integer.toString(3));
		typeValueDefultMap.put(Boolean.class.getName(), Integer.toString(4));
		typeValueDefultMap.put(int.class.getName(), Integer.toString(5));
		typeValueDefultMap.put(Integer.class.getName(), Integer.toString(6));
		typeValueDefultMap.put(long.class.getName(), Integer.toString(7));
		typeValueDefultMap.put(Long.class.getName(), Integer.toString(8));
		typeValueDefultMap.put(float.class.getName(), Integer.toString(9));
		typeValueDefultMap.put(Float.class.getName(), Integer.toString(10));
		typeValueDefultMap.put(double.class.getName(), Integer.toString(11));
		typeValueDefultMap.put(Double.class.getName(), Integer.toString(12));
		typeValueDefultMap.put(char.class.getName(), Integer.toString(13));
		typeValueDefultMap.put(String.class.getName(), Integer.toString(14));
		typeValueDefultMap.put(Date.class.getName(), Integer.toString(15));
		typeValueDefultMap.put(BigDecimal.class.getName(), Integer.toString(16));
		typeValueDefultMap.put(ArrayList.class.getName(), Integer.toString(17));
		typeValueDefultMap.put(HashMap.class.getName(), Integer.toString(18));
		
		
		Iterator<String> it = typeValueDefultMap.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			String value = typeValueDefultMap.get(key);
			if(StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)){
				valueTypeDefultMap.put(value,key);
			}
		}
	}
	
}



