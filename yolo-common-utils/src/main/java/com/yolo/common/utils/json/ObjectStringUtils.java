package com.yolo.common.utils.json;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yolo.common.utils.DateTimeUtils;
import com.yolo.common.utils.StringUtils;
import com.yolo.common.utils.clas.ModleClassManager;

public class ObjectStringUtils {

	public static String objectTransformString(Object obj)throws Exception{
		String result = null;
		if(obj!=null){
			TreeBean treeBean = TransformAdapterFactory.getInstance().objectToTreeBean(obj);
			if(treeBean!=null){
				//获取类型对象
				TypeDictionary typeDictionary = ObjectStringUtils.getTypeDictionary(treeBean);
				//压缩类型数据
				ObjectStringUtils.encodedType(treeBean, typeDictionary);
				//转字符串
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("value", treeBean);
				Map<String, String> typeD = typeDictionary.getTypeValueMap();
				if(typeD!=null && !typeD.isEmpty()){
					jsonObject.put("typeD",typeD);
				}
				result = jsonObject.toJSONString();
			}
		}
		return result;
	}
	
	
	public static <T> T stringTransformObject(String jsonStr)throws Exception{
		T result = null;
		if(StringUtils.isNotBlank(jsonStr)){
			JSONObject josnObject =  JSON.parseObject(jsonStr);
			if(josnObject!=null){
				JSONObject value = josnObject.getJSONObject("value");
				if(value!=null){
					TreeBean treeBean = TreeBean.jsonTreeToTreeBean(value);
					if(treeBean!=null){
						//获取类型
						JSONObject typeD = josnObject.getJSONObject("typeD");
						Map<String, String> map = ObjectStringUtils.getTypeValueMap(typeD);
						//解压类型
						TypeDictionary typeDictionary = new TypeDictionary();
						typeDictionary.addTypeValueMap(map);
						ObjectStringUtils.decodeType(treeBean, typeDictionary);
						//转换对象
						Object obj = TransformAdapterFactory.getInstance().treeBeanToObject(treeBean);
						@SuppressWarnings("unchecked")
						T t =  (T)obj;
						result = t;
					}
				}
			}
		}
		return result;
	}
	
	private static Map<String, String> getTypeValueMap(JSONObject typeD){
		Map<String, String> map = null;
		if(typeD!=null){
			map = new HashMap<String, String>();
			Iterator<String> it = typeD.keySet().iterator();
			while(it.hasNext()){
				String key = it.next();
				String value = typeD.getString(key);
				map.put(key, value);
			}
		}
		return map;
	}
	
	
	private static TypeDictionary getTypeDictionary(TreeBean treeBean) throws Exception{
		TypeDictionary typeDictionary = null;
		if(treeBean!=null){
			typeDictionary = new TypeDictionary();
			ITreeBeanProcess treeBeanProcess = new ITreeBeanProcess() {
				public void treeBeanProcess(TreeBean treeBean, TypeDictionary typeDictionary)
						throws Exception {
					String t = treeBean.getT();
					typeDictionary.setType(t);
				}
			};
			ObjectStringUtils.treeBeanProcess(treeBean,typeDictionary,treeBeanProcess);
		}
		return typeDictionary;
	}
	
	
	private static void treeBeanProcess(TreeBean treeBean,TypeDictionary typeDictionary,ITreeBeanProcess treeBeanProess)throws Exception{
		if(treeBean!=null && typeDictionary!=null){
			treeBeanProess.treeBeanProcess(treeBean, typeDictionary);
			TreeBean b = treeBean.getB();
			List<TreeBean> l = treeBean.getL();
			Map<TreeBean,TreeBean> m = treeBean.getM();
			if(b!=null){
				ObjectStringUtils.treeBeanProcess(b, typeDictionary,treeBeanProess);
			}
			if(l!=null && !l.isEmpty()){
				for (TreeBean tBean : l) {
					ObjectStringUtils.treeBeanProcess(tBean, typeDictionary,treeBeanProess);
				}
			}
			if(m!=null && !m.isEmpty()){
				Iterator<TreeBean> it = m.keySet().iterator();
				while(it.hasNext()){
					TreeBean key = it.next();
					TreeBean value = m.get(key);
					ObjectStringUtils.treeBeanProcess(key, typeDictionary,treeBeanProess);
					ObjectStringUtils.treeBeanProcess(value, typeDictionary,treeBeanProess);
				}
			}
		}
	}
	
	
	private static void encodedType(TreeBean treeBean,TypeDictionary typeDictionary)throws Exception{
		if(treeBean!=null && typeDictionary!=null){
			ITreeBeanProcess treeBeanProcess = new ITreeBeanProcess() {
				public void treeBeanProcess(TreeBean treeBean, TypeDictionary typeDictionary)
						throws Exception {
					String t = treeBean.getT();
					treeBean.setT(typeDictionary.getValueByType(t));
				}
			};
			ObjectStringUtils.treeBeanProcess(treeBean, typeDictionary,treeBeanProcess);
		}
	}
	
	
	private static void decodeType(TreeBean treeBean,TypeDictionary typeDictionary)throws Exception{
		if(treeBean!=null && typeDictionary!=null){
			ITreeBeanProcess treeBeanProess = new ITreeBeanProcess() {
				public void treeBeanProcess(TreeBean treeBean, TypeDictionary typeDictionary)
						throws Exception {
					String t = treeBean.getT();
					treeBean.setT(typeDictionary.getTypeByValue(t));
				}
			};
			ObjectStringUtils.treeBeanProcess(treeBean, typeDictionary,treeBeanProess);
		}
	}
	
}

interface ITreeBeanProcess{
	public void treeBeanProcess(TreeBean treeBean,TypeDictionary typeDictionary)throws Exception;
}


interface ITransform{
	public TreeBean objectToTreeBean(Object obj)throws Exception;
	public Object treeBeanToObject(TreeBean tree)throws Exception;
}

abstract class TransformHandle implements ITransform {
	private TransformAdapter transformAdapter;
	
	public TransformAdapter getTransformAdapter() {
		return transformAdapter;
	}

	public void setTransformAdapter(TransformAdapter transformAdapter) {
		this.transformAdapter = transformAdapter;
	}

	private void checkClass(String classValue)throws Exception{
		if(!this.isTransformClass(classValue)){
			throw new Exception("处理类型校验失败,class:"+classValue);
		}
	}
	
	public TreeBean objectToTreeBean(Object obj)throws Exception{
		TreeBean treeBean = null;
		if(obj!=null){
			this.checkClass(obj.getClass().getName());
			treeBean = this.objectToTreeBeanTransform(obj);
		}
		return treeBean;
	}
	public Object treeBeanToObject(TreeBean tree)throws Exception{
		Object obj = null;
		if(tree!=null){
			String type = tree.getT();
			this.checkClass(type);
			obj = this.treeBeanToObjectTransform(tree);
		}
		return obj;
	}
	
	public abstract boolean isTransformClass(String clas)throws Exception;
	protected abstract TreeBean objectToTreeBeanTransform(Object obj)throws Exception;
	protected abstract Object treeBeanToObjectTransform(TreeBean tree)throws Exception;
}



/**
 * 基本类型处理
 *
 */
class TransformBaseClassHandle extends TransformHandle{

	public boolean isTransformClass(String className) throws Exception {
		boolean flag = false;
		if(StringUtils.isNotBlank(className)){
			BaseTypeDo baseTypeDo = BASE_TYPE_DO_MAP.get(className);
			if(baseTypeDo!=null){
				flag = baseTypeDo.isThisType(className);
			}
		}
		return flag;
	}

	@Override
	protected TreeBean objectToTreeBeanTransform(Object obj) throws Exception {
		TreeBean treeBean = null;
		if(obj!=null){
			String className = obj.getClass().getName();
			BaseTypeDo baseTypeDo = BASE_TYPE_DO_MAP.get(className);
			String value = baseTypeDo.baseValueToString(obj);
			treeBean = new TreeBean();
			treeBean.setT(obj.getClass().getName());
			treeBean.setV(value);
		}
		return treeBean;
	}

	@Override
	protected Object treeBeanToObjectTransform(TreeBean tree) throws Exception {
		Object value = null;
		if(tree!=null){
			String t = tree.getT();
			String v = tree.getV();
			BaseTypeDo baseTypeDo = BASE_TYPE_DO_MAP.get(t);
			value = baseTypeDo.stringToBaseValue(v);
		}
		return value;
	}

	abstract static class BaseTypeDo{
		private List<String> classNameList;

		public BaseTypeDo(String... classNames){
			if(classNames!=null && classNames.length>0){
				this.classNameList = new ArrayList<String>();
				for (int i=0;i<classNames.length;i++){
					this.classNameList.add(classNames[i]);
				}
			}
		}

		public List<String> getBaseTypeNameList(){
			return this.classNameList;
		}

		public boolean isThisType(String className){
			boolean isThisType = false;
			if(classNameList!=null && classNameList.size()>0 &&
					StringUtils.isNotBlank(className) && classNameList.contains(className)){
				isThisType = true;
			}
			return isThisType;
		}

		public String baseValueToString(Object object)throws Exception{
			String result = null;
			if(object!=null){
				result = this.baseValueToStringDefault(object);
			}
			return result;
		}
		public Object stringToBaseValue(String value)throws Exception{
			Object result = null;
			if(StringUtils.isNotBlank(value)){
				result = this.stringToBaseValueDefault(value);
			}
			return result;
		}
		protected String baseValueToStringDefault(Object object)throws Exception{
			return object.toString();
		}
		protected Object stringToBaseValueDefault(String value)throws Exception{
			return value;
		}

	}
	private static Map<String,BaseTypeDo> BASE_TYPE_DO_MAP = new HashMap<String, BaseTypeDo>();
	static{
		List<BaseTypeDo> list =new ArrayList<BaseTypeDo>();
		BaseTypeDo stringType = new BaseTypeDo(String.class.getName()){
			public Object stringToBaseValue(String value) throws Exception {
				return value;
			}
		};
		list.add(stringType);
		BaseTypeDo intType = new BaseTypeDo(int.class.getName(),Integer.class.getName()){
			public Object stringToBaseValueDefault(String value) throws Exception {
				return Integer.parseInt(value);
			}
		};
		list.add(intType);
		BaseTypeDo longType = new BaseTypeDo(long.class.getName(),Long.class.getName()){
			public Object stringToBaseValueDefault(String value) throws Exception {
				return Long.parseLong(value);
			}
		};
		list.add(longType);
		BaseTypeDo floatType = new BaseTypeDo(float.class.getName(),Float.class.getName()){
			public Object stringToBaseValueDefault(String value) throws Exception {
				return Float.parseFloat(value);
			}
		};
		list.add(floatType);
		BaseTypeDo doubleType = new BaseTypeDo(double.class.getName(),Double.class.getName()){
			public Object stringToBaseValueDefault(String value) throws Exception {
				return Double.parseDouble(value);
			}
		};
		list.add(doubleType);
		BaseTypeDo bigDecimalType = new BaseTypeDo(BigDecimal.class.getName()){
			public Object stringToBaseValueDefault(String value) throws Exception {
				return new BigDecimal(value);
			}
		};
		list.add(bigDecimalType);
		BaseTypeDo dateType = new BaseTypeDo(Date.class.getName()){
			public String baseValueToStringDefault(Object object)throws Exception{
				return DateTimeUtils.dateFormatString((Date)object, DateTimeUtils.FormatType.FORMAT_Y_M_D_H_M_S_S_1);
			}
			public Object stringToBaseValueDefault(String value) throws Exception {
				return DateTimeUtils.stringFormatDate(value, DateTimeUtils.FormatType.FORMAT_Y_M_D_H_M_S_S_1);
			}
		};
		list.add(dateType);
		BaseTypeDo booleanType = new BaseTypeDo(boolean.class.getName(),Boolean.class.getName()){
			public Object stringToBaseValueDefault(String value) throws Exception {
				return Boolean.parseBoolean(value);
			}
		};
		list.add(booleanType);
		BaseTypeDo byteType = new BaseTypeDo(byte.class.getName(),Byte.class.getName()){
			public Object stringToBaseValueDefault(String value) throws Exception {
				return Byte.parseByte(value);
			}
		};
		list.add(byteType);
		BaseTypeDo charType = new BaseTypeDo(char.class.getName()){
			public Object stringToBaseValueDefault(String value) throws Exception {
				Object result = null;
				char[] charArray=value.toString().toCharArray();
				if(charArray!=null && charArray.length>0){
					result=charArray[0];
				}
				return result;
			}
		};
		list.add(charType);
		for (BaseTypeDo item : list) {
			List<String> nameListItem = item.getBaseTypeNameList();
			for (String type :nameListItem) {
				BASE_TYPE_DO_MAP.put(type,item);
			}
		}
	}
}


/**
 * 数组
 *
 */
class TransformArrayHandle extends TransformHandle{

	@Override
	public boolean isTransformClass(String classValue) throws Exception {
		boolean flag = false;
		if(StringUtils.isNotBlank(classValue)){
			Class<?> classT=Class.forName(classValue);
			if(classT.isArray()){
				flag = true;
			}
		}
		return flag;
	}

	@Override
	protected TreeBean objectToTreeBeanTransform(Object obj) throws Exception {
		TreeBean treeBean = null;
		if(obj!=null){
			Object[] array=(Object[])obj;
			if(array!=null){
				List<TreeBean> list = new ArrayList<TreeBean>();
				for (Object item : array) {
					TreeBean treeBeanItem = this.getTransformAdapter().objectToTreeBean(item);
					list.add(treeBeanItem);
				}
				treeBean = new TreeBean();
				treeBean.setT(obj.getClass().getName());
				treeBean.setL(list);
			}
		}
		return treeBean;
	}

	@Override
	protected Object treeBeanToObjectTransform(TreeBean tree) throws Exception {
		Object result = null;
		if(tree!=null){
			String type = tree.getT();
			List<TreeBean> list = tree.getL();
			if(list!=null){
				Class<?> classType=Class.forName(type);
				Class<?> classItem = classType.getComponentType();
				Object array = Array.newInstance(classItem, list.size());
				for (int i = 0; i < list.size(); i++) {
					Object objectItem = this.getTransformAdapter().treeBeanToObject(list.get(i));
					Array.set(array, i, objectItem);
				}
				result = array;
			}
		}
		return result;
	}
}


/**
 * List
 *
 */
class TransformListHandle extends TransformHandle{

	@Override
	public boolean isTransformClass(String classValue) throws Exception {
		boolean flag = false;
		if(StringUtils.isNotBlank(classValue)){
			Class<?> classT=Class.forName(classValue);
			if(List.class.isAssignableFrom(classT)){
				flag = true;
			}
		}
		return flag;
	}

	@Override
	protected TreeBean objectToTreeBeanTransform(Object obj) throws Exception {
		TreeBean treeBean = null;
		if(obj!=null){
			List<?> list=(List<?>)obj;
			List<TreeBean> treeBeanList = new ArrayList<TreeBean>();
			for (Object item : list) {
				TreeBean treeBeanItem = this.getTransformAdapter().objectToTreeBean(item);
				treeBeanList.add(treeBeanItem);
			}
			String className = obj.getClass().getName();
			treeBean = new TreeBean();
			treeBean.setT(className);
			treeBean.setL(treeBeanList);
		}
		return treeBean;
	}

	@Override
	protected Object treeBeanToObjectTransform(TreeBean tree) throws Exception {
		Object result = null;
		if(tree!=null){
			String type = tree.getT();
			List<TreeBean> treeBeanList = tree.getL();
			if(treeBeanList!=null){
				Class<?> classType=Class.forName(type);
				Object object = classType.newInstance();
				@SuppressWarnings("unchecked")
				List<Object> list=(List<Object>)object;
				for (TreeBean treeBeanItem : treeBeanList) {
					Object objectItem = this.getTransformAdapter().treeBeanToObject(treeBeanItem);
					list.add(objectItem);
				}
				result = list;
			}
		}
		return result;
	}
}


/**
 * Map
 *
 */
class TransformMapHandle extends TransformHandle{

	@Override
	public boolean isTransformClass(String classValue) throws Exception {
		boolean flag = false;
		if(StringUtils.isNotBlank(classValue)){
			Class<?> classT=Class.forName(classValue);
			if(Map.class.isAssignableFrom(classT)){
				flag = true;
			}
		}
		return flag;
	}

	@Override
	protected TreeBean objectToTreeBeanTransform(Object obj) throws Exception {
		TreeBean result = null;
		if(obj!=null){
			Map<?,?> map=(Map<?,?>)obj;
			Map<TreeBean,TreeBean> valueMap=new HashMap<TreeBean,TreeBean>();
			Iterator<?> it=map.keySet().iterator();
			while (it.hasNext()) {
				Object key=it.next();
				Object valueObject=map.get(key);
				TreeBean jObjK=this.getTransformAdapter().objectToTreeBean(key);
				TreeBean jObjV=this.getTransformAdapter().objectToTreeBean(valueObject);
				if(jObjK!=null){
					valueMap.put(jObjK, jObjV);
				}
			}
			if(valueMap!=null){
				result = new TreeBean();
				result.setT(obj.getClass().getName());
				result.setM(valueMap);
			}
		}
		return result;
	}

	@Override
	protected Object treeBeanToObjectTransform(TreeBean tree) throws Exception {
		Object result = null;
		if(tree!=null){
			String type = tree.getT();
			Map<TreeBean,TreeBean> valueMap= tree.getM();
			if(valueMap!=null){
				Class<?> classType=Class.forName(type);
				Object object = classType.newInstance();
				@SuppressWarnings("unchecked")
				Map<Object,Object> map=(Map<Object,Object>)object;
				Iterator<TreeBean> it=valueMap.keySet().iterator();
				while(it.hasNext()){
					TreeBean keyItem=it.next();
					TreeBean valueItem=valueMap.get(keyItem);
					Object keyObject = this.getTransformAdapter().treeBeanToObject(keyItem);
					Object valueObject = this.getTransformAdapter().treeBeanToObject(valueItem);
					if(keyObject!=null){
						map.put(keyObject, valueObject);
					}
				}
				result = map;
			}
			
		}
		return result;
	}
	
}


/**
 * 枚举类型
 *
 */
class TransformEnumHandle extends TransformHandle{

	@Override
	public boolean isTransformClass(String classValue) throws Exception {
		boolean flag = false;
		if(StringUtils.isNotBlank(classValue)){
			Class<?> classT=Class.forName(classValue);
			if(classT.isEnum()){
				flag = true;
			}
		}
		return flag;
	}

	@Override
	protected TreeBean objectToTreeBeanTransform(Object obj) throws Exception {
		TreeBean treeBean = null;
		if(obj!=null){
			String value = obj.toString();
			treeBean = new TreeBean();
			treeBean.setT(obj.getClass().getName());
			treeBean.setV(value);
		}
		return treeBean;
	}

	@Override
	protected Object treeBeanToObjectTransform(TreeBean tree) throws Exception {
		Object result = null;
		if(tree!=null){
			String type = tree.getT();
			String value = tree.getV();
			Class<?> classType = Class.forName(type);
			Enum<?>[] enumArray = (Enum<?>[])classType.getEnumConstants();
			Enum<?> enumValue = this.getEnum(enumArray, value);
			result = enumValue;
		}
		return result;
	}

	private Enum<?> getEnum(Enum<?>[] enumArray, String name) {
		Enum<?> value = null;
		if(enumArray!=null && enumArray.length>0){
			for (Enum<?> enumItem : enumArray) {
				String nameItem = enumItem.name();
				if(nameItem.equals(name)){
					value = enumItem;
					break;
				}
			}
		}
		return value;
	}
}


/**
 * 数据实体Model
 *
 */
class TransformModelHandle extends TransformHandle{

	private static Logger logger = LoggerFactory.getLogger(TransformModelHandle.class);
	
	@Override
	public boolean isTransformClass(String classValue) throws Exception {
		boolean flag = true;
		return flag;
	}

	@Override
	protected TreeBean objectToTreeBeanTransform(Object obj) throws Exception {
		TreeBean result = null;
		if(obj!=null){
			Map<TreeBean,TreeBean> valueMap=new HashMap<TreeBean,TreeBean>();
			List<Field> fieldList=ModleClassManager.getFieldListExceptStatic(obj.getClass());
			if(fieldList!=null && !fieldList.isEmpty()){
				for (Field field : fieldList) {
					Object fieldValue = field.get(obj);
					if(fieldValue!=null){
						String fieldName=field.getName();
						Method methodGet = ModleClassManager.getMethodGet(obj.getClass(), fieldName);
						if(methodGet!=null && Modifier.isPublic(methodGet.getModifiers())){
							TreeBean nameTreeBean = this.getTransformAdapter().objectToTreeBean(fieldName);
							TreeBean valueTreeBean = this.getTransformAdapter().objectToTreeBean(fieldValue);
							if(nameTreeBean!=null && valueTreeBean!=null){
								valueMap.put(nameTreeBean, valueTreeBean);
							}
							
						}
					}
				}
			}
			if(!valueMap.isEmpty()){
				result = new TreeBean();
				result.setT(obj.getClass().getName());
				result.setM(valueMap);
			}
		}
		return result;
	}

	@Override
	protected Object treeBeanToObjectTransform(TreeBean tree) throws Exception {
		Object result = null;
		if(tree!=null){
			String type = tree.getT();
			Map<TreeBean,TreeBean> valueMap= tree.getM();
			if(valueMap!=null){
				Class<?> classType=Class.forName(type);
				Object object = classType.newInstance();
				Iterator<TreeBean> it=valueMap.keySet().iterator();
				while(it.hasNext()){
					TreeBean keyItem=it.next();
					TreeBean valueItem=valueMap.get(keyItem);
					Object keyObject = this.getTransformAdapter().treeBeanToObject(keyItem);
					Object valueObject = this.getTransformAdapter().treeBeanToObject(valueItem);
					if(keyObject!=null && valueObject!=null){
						Method methodSet = ModleClassManager.getMethodSet(classType, keyObject.toString());
						if(methodSet!=null && Modifier.isPublic(methodSet.getModifiers())){
							try {
								methodSet.invoke(object, valueObject);
							} catch (Exception e) {
								logger.error("fieldName:"+keyObject+",fieldValue:"+valueObject+",methodSet");
								e.printStackTrace();
								throw e;
							}
							
						}
					}
				}
				result = object;
			}
		}
		return result;
	}
	
}





class TransformAdapter implements ITransform{

	private List<TransformHandle> transformHandleList;
	private TransformHandle transformHandleAllOther;
	
	public TransformAdapter(List<TransformHandle> transformHandleList,TransformHandle transformHandleAllOther)throws Exception{
		if(transformHandleAllOther == null){
			throw new Exception();
		}
		this.transformHandleList = transformHandleList;
		this.transformHandleAllOther = transformHandleAllOther;
		
		if(this.transformHandleList!=null && !this.transformHandleList.isEmpty()){
			for (TransformHandle transformHandle : transformHandleList) {
				transformHandle.setTransformAdapter(this);
			}
		}
		this.transformHandleAllOther.setTransformAdapter(this);
	}
	
	
	private TransformHandle findTransformHandle(String clas)throws Exception{
		TransformHandle th = null;
		if(this.transformHandleList != null && !this.transformHandleList.isEmpty()){
			for (TransformHandle transformHandle : this.transformHandleList) {
				if(transformHandle.isTransformClass(clas)){
					th = transformHandle;
					break;
				}
			}
		}
		if(th == null){
			th = this.transformHandleAllOther;
		}
		if(th == null){
			throw new Exception("适配器中获取Handle处理对象失败");
		}
		return th;
	}

	public TreeBean objectToTreeBean(Object obj) throws Exception {
		TreeBean treeBean = null;
		if(obj!=null){
			TransformHandle transformHandle = this.findTransformHandle(obj.getClass().getName());
			if(transformHandle!=null){
				treeBean = transformHandle.objectToTreeBean(obj);
			}
		}
		return treeBean;
	}

	public Object treeBeanToObject(TreeBean tree) throws Exception {
		Object object = null;
		if(tree!=null){
			TransformHandle transformHandle = this.findTransformHandle(tree.getT());
			if(transformHandle!=null){
				object = transformHandle.treeBeanToObject(tree);
			}
		}
		return object;
	}
	
}


class TransformAdapterFactory{
	private static TransformAdapter transformAdapter;
	
	public static TransformAdapter getInstance()throws Exception{
		if(transformAdapter == null){
			synchronized (TransformAdapterFactory.class) {
				if(transformAdapter == null){
					createTransformAdapter();
				}
			}
		}
		return transformAdapter;
	}
	
	
	private static void createTransformAdapter()throws Exception{
		List<TransformHandle> handleList = new ArrayList<TransformHandle>();
		TransformBaseClassHandle transformBaseClassHandle = new TransformBaseClassHandle();
		TransformArrayHandle transformArrayHandle = new TransformArrayHandle();
		TransformListHandle transformListHandle = new TransformListHandle();
		TransformMapHandle transformMapHandle = new TransformMapHandle();
		TransformEnumHandle transformEnumHandle = new TransformEnumHandle();
		handleList.add(transformBaseClassHandle);
		handleList.add(transformArrayHandle);
		handleList.add(transformListHandle);
		handleList.add(transformMapHandle);
		handleList.add(transformEnumHandle);
		TransformModelHandle transformModelHandle = new TransformModelHandle();
		TransformAdapter transformAdapter = new TransformAdapter(handleList, transformModelHandle);
		TransformAdapterFactory.transformAdapter = transformAdapter;
	}
	
}





class TreeBean{
	private String t;
	private String v;
	private TreeBean b;
	private List<TreeBean> l;
	private Map<TreeBean,TreeBean> m;
	
	
	public static String treeBeanToString(TreeBean bean){
		String result = null;
		if(bean!=null){
			result = JSON.toJSONString(bean);
		}
		return result;
	}
	
	public static TreeBean jsonTreeToTreeBean(JSONObject jsonObject){
		TreeBean result = null;
		if(jsonObject!=null){
			result = TreeBean.getTreeBeanObject(jsonObject);
		}
		return result;
	}
	
	private static TreeBean getTreeBeanObject(JSONObject jsonObject){
		TreeBean treeBean = null;
		if(jsonObject!=null){
			String t=jsonObject.getString("t");
			String v=jsonObject.getString("v");
			JSONArray l=jsonObject.getJSONArray("l");
			JSONObject m=jsonObject.getJSONObject("m");
			
			treeBean = new TreeBean();
			treeBean.setT(t);
			treeBean.setV(v);
			if(l!=null){
				treeBean.setL(TreeBean.getTreeBeanList(l));
			}
			if(m!=null){
				treeBean.setM(TreeBean.getTreeBeanMap(m));
			}
		}
		return treeBean;
	}
	
	private static List<TreeBean> getTreeBeanList(JSONArray l){
		List<TreeBean> list = null;
		if(l!=null){
			list = new ArrayList<TreeBean>();
			for (int i = 0; i < l.size(); i++) {
				JSONObject obj = l.getJSONObject(i);
				TreeBean treeBean = TreeBean.getTreeBeanObject(obj);
				list.add(treeBean);
			}
		}
		return list;
	}
	
	private static Map<TreeBean,TreeBean> getTreeBeanMap(JSONObject m){
		Map<TreeBean,TreeBean> map = null;
		if(m!=null){
			map = new HashMap<TreeBean, TreeBean>();
			Iterator<String> it = m.keySet().iterator();
			while(it.hasNext()){
				String key = it.next();
				JSONObject value = m.getJSONObject(key);
				JSONObject jsonKey = JSON.parseObject(key);
				TreeBean treeBeanKey = TreeBean.getTreeBeanObject(jsonKey);
				TreeBean treeBeanValue = TreeBean.getTreeBeanObject(value);
				map.put(treeBeanKey, treeBeanValue);
			}
		}
		return map;
	}
	
	
	
	
	public String getT() {
		return t;
	}
	public void setT(String t) {
		this.t = t;
	}
	public String getV() {
		return v;
	}
	public void setV(String v) {
		this.v = v;
	}
	public TreeBean getB() {
		return b;
	}
	public void setB(TreeBean b) {
		this.b = b;
	}
	public List<TreeBean> getL() {
		return l;
	}
	public void setL(List<TreeBean> l) {
		this.l = l;
	}
	public Map<TreeBean, TreeBean> getM() {
		return m;
	}
	public void setM(Map<TreeBean, TreeBean> m) {
		this.m = m;
	}
	@Override
	public String toString() {
		return "TreeBean [t=" + t + ", v=" + v + ", b=" + b + ", l=" + l
				+ ", m=" + m + "]";
	}
}




class TypeDictionary{
	private static TypeDictionaryValue typeDictionaryValueDefault = new TypeDictionaryValue();
	private TypeDictionaryValue typeDictionaryValue = new  TypeDictionaryValue();
	static {
		initDefault();
	}

	public void addTypeValueMap(Map<String,String> map)throws Exception{
		if(map !=null && !map.isEmpty()){
			Iterator<String> it = map.keySet().iterator();
			while(it.hasNext()){
				String key = it.next();
				String value = map.get(key);
				if(StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)){
					typeDictionaryValue.addTypeDictionaryValue(key, value);
				}
			}
		}
	}

	public void setType(String type)throws Exception{
		if(StringUtils.isNotBlank(type)){
			if(typeDictionaryValueDefault.getValueByType(type) == null && typeDictionaryValue.getValueByType(type) == null){
				int size = typeDictionaryValueDefault.getTypeDictionaryValueSize() + typeDictionaryValue.getTypeDictionaryValueSize();
				String value = Integer.toString(++size);
				typeDictionaryValue.addTypeDictionaryValue(type,value);
			}
		}
	}
	
	public String getValueByType(String type){
		String value = typeDictionaryValueDefault.getValueByType(type);
		if(value==null){
			value = typeDictionaryValue.getValueByType(type);
		}
		return value;
	}
	public String getTypeByValue(String value){
		String type = typeDictionaryValueDefault.getTypeByValue(value);
		if(type == null){
			type = typeDictionaryValue.getTypeByValue(value);
		}
		return type;
	}

	public Map<String, String> getTypeValueMap() {
		return typeDictionaryValue.getTypeValueMap();
	}

	private static void  initDefault(){
		try {
			typeDictionaryValueDefault.addTypeDictionaryValue(String.class.getName(), Integer.toString(1));
			typeDictionaryValueDefault.addTypeDictionaryValue(Byte.class.getName(), Integer.toString(2));
			typeDictionaryValueDefault.addTypeDictionaryValue(Integer.class.getName(), Integer.toString(3));
			typeDictionaryValueDefault.addTypeDictionaryValue(Long.class.getName(), Integer.toString(4));
			typeDictionaryValueDefault.addTypeDictionaryValue(Float.class.getName(), Integer.toString(5));
			typeDictionaryValueDefault.addTypeDictionaryValue(Double.class.getName(), Integer.toString(6));
			typeDictionaryValueDefault.addTypeDictionaryValue(BigDecimal.class.getName(), Integer.toString(7));
			typeDictionaryValueDefault.addTypeDictionaryValue(Boolean.class.getName(), Integer.toString(8));
			typeDictionaryValueDefault.addTypeDictionaryValue(Date.class.getName(), Integer.toString(9));
			typeDictionaryValueDefault.addTypeDictionaryValue(ArrayList.class.getName(), Integer.toString(10));
			typeDictionaryValueDefault.addTypeDictionaryValue(HashMap.class.getName(), Integer.toString(11));
			typeDictionaryValueDefault.addTypeDictionaryValue(byte.class.getName(), Integer.toString(12));
			typeDictionaryValueDefault.addTypeDictionaryValue(int.class.getName(), Integer.toString(13));
			typeDictionaryValueDefault.addTypeDictionaryValue(long.class.getName(), Integer.toString(14));
			typeDictionaryValueDefault.addTypeDictionaryValue(float.class.getName(), Integer.toString(15));
			typeDictionaryValueDefault.addTypeDictionaryValue(double.class.getName(), Integer.toString(16));
			typeDictionaryValueDefault.addTypeDictionaryValue(boolean.class.getName(), Integer.toString(17));
			typeDictionaryValueDefault.addTypeDictionaryValue(char.class.getName(), Integer.toString(18));
		}catch (Exception e){
			e.printStackTrace();
		}
	}
}

class TypeDictionaryValue{
	private Map<String,String> typeValueMap = new HashMap<String, String>();
	private Map<String,String> valueTypeMap = new HashMap<String, String>();

	public String getValueByType(String type){
		return typeValueMap.get(type);
	}

	public String getTypeByValue(String value){
		return valueTypeMap.get(value);
	}

	public void addTypeDictionaryValue(String type,String value)throws Exception{
		String valueItem = typeValueMap.get(type);
		String classNameItem = valueTypeMap.get(value);
		if(valueItem == null && classNameItem == null){
			typeValueMap.put(type,value);
			valueTypeMap.put(value,type);
		}else{
			throw new Exception("已经存在该类型字典数据");
		}
	}
	public int getTypeDictionaryValueSize(){
		return typeValueMap.size();
	}

	public Map<String, String> getTypeValueMap() {
		return typeValueMap;
	}

	public Map<String, String> getValueTypeMap() {
		return valueTypeMap;
	}
}
