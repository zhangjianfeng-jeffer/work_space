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
	
	
	public static <T> T stringToObject(String jsonStr)throws Exception{
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
			ITreeBeanProess treeBeanProess = new ITreeBeanProess() {
				public void treeBeanProess(TreeBean treeBean, TypeDictionary typeDictionary)
						throws Exception {
					String t = treeBean.getT();
					typeDictionary.setType(t);
				}
			};
			ObjectStringUtils.treeBeanProess(treeBean,typeDictionary,treeBeanProess);
		}
		return typeDictionary;
	}
	
	
	private static void treeBeanProess(TreeBean treeBean,TypeDictionary typeDictionary,ITreeBeanProess treeBeanProess)throws Exception{
		if(treeBean!=null && typeDictionary!=null){
			treeBeanProess.treeBeanProess(treeBean, typeDictionary);
			TreeBean b = treeBean.getB();
			List<TreeBean> l = treeBean.getL();
			Map<TreeBean,TreeBean> m = treeBean.getM();
			if(b!=null){
				ObjectStringUtils.treeBeanProess(b, typeDictionary,treeBeanProess);
			}
			if(l!=null && !l.isEmpty()){
				for (TreeBean tBean : l) {
					ObjectStringUtils.treeBeanProess(tBean, typeDictionary,treeBeanProess);
				}
			}
			if(m!=null && !m.isEmpty()){
				Iterator<TreeBean> it = m.keySet().iterator();
				while(it.hasNext()){
					TreeBean key = it.next();
					TreeBean value = m.get(key);
					ObjectStringUtils.treeBeanProess(key, typeDictionary,treeBeanProess);
					ObjectStringUtils.treeBeanProess(value, typeDictionary,treeBeanProess);
				}
			}
		}
	}
	
	
	private static void encodedType(TreeBean treeBean,TypeDictionary typeDictionary)throws Exception{
		if(treeBean!=null && typeDictionary!=null){
			ITreeBeanProess treeBeanProess = new ITreeBeanProess() {
				public void treeBeanProess(TreeBean treeBean, TypeDictionary typeDictionary)
						throws Exception {
					String t = treeBean.getT();
					treeBean.setT(typeDictionary.getValueByType(t));
				}
			};
			ObjectStringUtils.treeBeanProess(treeBean, typeDictionary,treeBeanProess);
		}
	}
	
	
	private static void decodeType(TreeBean treeBean,TypeDictionary typeDictionary)throws Exception{
		if(treeBean!=null && typeDictionary!=null){
			ITreeBeanProess treeBeanProess = new ITreeBeanProess() {
				public void treeBeanProess(TreeBean treeBean, TypeDictionary typeDictionary)
						throws Exception {
					String t = treeBean.getT();
					treeBean.setT(typeDictionary.getTypeByValue(t));
				}
			};
			ObjectStringUtils.treeBeanProess(treeBean, typeDictionary,treeBeanProess);
		}
	}
	
}

interface ITreeBeanProess{
	public void treeBeanProess(TreeBean treeBean,TypeDictionary typeDictionary)throws Exception;
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

	private void checkClass(String clas)throws Exception{
		if(!this.isTransformClass(clas)){
			throw new Exception("处理类型校验失败,class:"+clas);
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

	public boolean isTransformClass(String clas) throws Exception {
		boolean flag = false;
		if(StringUtils.isNotBlank(clas)){
			flag = this.isBaseType(clas);
		}
		return flag;
	}

	@Override
	protected TreeBean objectToTreeBeanTransform(Object obj) throws Exception {
		TreeBean treeBean = null;
		if(obj!=null){
			String value = this.getBaseValueToString(obj);
			if(value == null){
				throw new Exception();
			}
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
			value = this.getStringToBaseValue(v, t);
		}
		return value;
	}
	
	
	private boolean isBaseType(String clas){
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
	
	
	private String getBaseValueToString(Object value)throws Exception{
		String result=null;
		if(value!=null){
			Class<?> clas=value.getClass();
			if(clas.equals(byte.class)||clas.equals(Byte.class)){
				result=value.toString();
			}else if(clas.equals(boolean.class)||clas.equals(Boolean.class)){
				result=value.toString();
			}else if(clas.equals(int.class)||clas.equals(Integer.class)){
				result=value.toString();
			}else if(clas.equals(long.class)||clas.equals(Long.class)){
				result=value.toString();
			}else if(clas.equals(float.class)||clas.equals(Float.class)){
				result=value.toString();
			}else if(clas.equals(double.class)||clas.equals(Double.class)){
				result=value.toString();
			}else if(clas.equals(char.class)){
				result=value.toString();
			}else if(clas.equals(String.class)){
				result=value.toString();
			}else if(clas.equals(Date.class)){
				result=DateTimeUtils.dateFormatString((Date)value, DateTimeUtils.FormatType.FORMAT_Y_M_D_H_M_S_S_1);
			}else if(clas.equals(BigDecimal.class)){
				result=value.toString();
			}
		}
		return result;
	}
	
	
	private Object getStringToBaseValue(String value,String type)throws Exception{
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
}


/**
 * 数组
 *
 */
class TransformArrayHandle extends TransformHandle{

	@Override
	public boolean isTransformClass(String clas) throws Exception {
		boolean flag = false;
		if(StringUtils.isNotBlank(clas) && clas.indexOf("[L")==0){
			flag = true;
		}
		return flag;
	}

	@Override
	protected TreeBean objectToTreeBeanTransform(Object obj) throws Exception {
		TreeBean treeBean = null;
		if(obj!=null){
			Object[] array=(Object[])obj;
			if(array!=null && array.length>0){
				List<TreeBean> list = new ArrayList<TreeBean>();
				for (Object item : array) {
					TreeBean treeBeanItem = this.getTransformAdapter().objectToTreeBean(item);
					if(treeBeanItem!=null){
						list.add(treeBeanItem);
					}
				}
				if(!list.isEmpty()){
					treeBean = new TreeBean();
					treeBean.setT(obj.getClass().getName());
					treeBean.setL(list);
				}
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
			if(list!=null && !list.isEmpty()){
				String typeItem=type.replace("[L", "");
				typeItem=typeItem.replace(";", "");
				Class<?> clasItem=Class.forName(typeItem);
				Object array = Array.newInstance(clasItem, list.size());
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
	public boolean isTransformClass(String clas) throws Exception {
		boolean flag = false;
		if(StringUtils.isNotBlank(clas)){
			Class<?> classT=Class.forName(clas);
			Object object = classT.newInstance();
			if(object instanceof List){
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
			if(!list.isEmpty()){
				List<TreeBean> treeBeanList = new ArrayList<TreeBean>();
				for (Object item : list) {
					TreeBean treeBeanItem = this.getTransformAdapter().objectToTreeBean(item);
					if(treeBeanItem!=null){
						treeBeanList.add(treeBeanItem);
					}
				}
				treeBean = new TreeBean();
				treeBean.setT(obj.getClass().getName());
				treeBean.setL(treeBeanList);
			}
		}
		return treeBean;
	}

	@Override
	protected Object treeBeanToObjectTransform(TreeBean tree) throws Exception {
		Object result = null;
		if(tree!=null){
			String type = tree.getT();
			List<TreeBean> treeBeanList = tree.getL();
			if(treeBeanList!=null && !treeBeanList.isEmpty()){
				Class<?> clas=Class.forName(type);
				Object object = clas.newInstance();
				@SuppressWarnings("unchecked")
				List<Object> list=(List<Object>)object;
				for (TreeBean treeBeanItem : treeBeanList) {
					Object objectItem = this.getTransformAdapter().treeBeanToObject(treeBeanItem);
					if(objectItem!=null){
						list.add(objectItem);
					}
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
	public boolean isTransformClass(String clas) throws Exception {
		boolean flag = false;
		if(StringUtils.isNotBlank(clas)){
			Class<?> classT=Class.forName(clas);
			Object object = classT.newInstance();
			if(object instanceof Map){
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
				if(jObjK!=null && jObjV!=null){
					valueMap.put(jObjK, jObjV);
				}
			}
			if(valueMap!=null && !valueMap.isEmpty()){
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
				Class<?> clas=Class.forName(type);
				Object object = clas.newInstance();
				@SuppressWarnings("unchecked")
				Map<Object,Object> map=(Map<Object,Object>)object;
				Iterator<TreeBean> it=valueMap.keySet().iterator();
				while(it.hasNext()){
					TreeBean keyItem=it.next();
					TreeBean valueItem=valueMap.get(keyItem);
					Object keyObject = this.getTransformAdapter().treeBeanToObject(keyItem);
					Object valueObject = this.getTransformAdapter().treeBeanToObject(valueItem);
					if(keyObject!=null && valueObject!=null){
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
 * 数据实体Model
 *
 */
class TransformModelHandle extends TransformHandle{

	private static Logger logger = LoggerFactory.getLogger(TransformModelHandle.class);
	
	@Override
	public boolean isTransformClass(String clas) throws Exception {
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
				Class<?> clas=Class.forName(type);
				Object object = clas.newInstance();
				Iterator<TreeBean> it=valueMap.keySet().iterator();
				while(it.hasNext()){
					TreeBean keyItem=it.next();
					TreeBean valueItem=valueMap.get(keyItem);
					Object keyObject = this.getTransformAdapter().treeBeanToObject(keyItem);
					Object valueObject = this.getTransformAdapter().treeBeanToObject(valueItem);
					if(keyObject!=null && valueObject!=null){
						Method methodSet = ModleClassManager.getMethodSet(clas, keyObject.toString());
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
		handleList.add(transformBaseClassHandle);
		handleList.add(transformArrayHandle);
		handleList.add(transformListHandle);
		handleList.add(transformMapHandle);
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
	
	public static TreeBean jsonTreeToTreeBean(JSONObject josnObject){
		TreeBean result = null;
		if(josnObject!=null){
			result = TreeBean.getTreeBeanObject(josnObject);
		}
		return result;
	}
	
	private static TreeBean getTreeBeanObject(JSONObject josnObject){
		TreeBean treeBean = null;
		if(josnObject!=null){
			String t=josnObject.getString("t");
			String v=josnObject.getString("v");
			JSONArray l=josnObject.getJSONArray("l");
			JSONObject m=josnObject.getJSONObject("m");
			
			treeBean = new TreeBean();
			treeBean.setT(t);
			treeBean.setV(v);
			if(l!=null && l.size()>0){
				treeBean.setL(TreeBean.getTreeBeanList(l));
			}
			if(m!=null && m.size()>0){
				treeBean.setM(TreeBean.getTreeBeanMap(m));
			}
		}
		return treeBean;
	}
	
	private static List<TreeBean> getTreeBeanList(JSONArray l){
		List<TreeBean> list = null;
		if(l!=null && l.size()>0){
			list = new ArrayList<TreeBean>();
			for (int i = 0; i < l.size(); i++) {
				JSONObject obj = l.getJSONObject(i);
				TreeBean treeBean = TreeBean.getTreeBeanObject(obj);
				if(treeBean!=null){
					list.add(treeBean);
				}
			}
		}
		return list;
	}
	
	private static Map<TreeBean,TreeBean> getTreeBeanMap(JSONObject m){
		Map<TreeBean,TreeBean> map = null;
		if(m!=null && m.size()>0){
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
	private int index;
	private Map<String,String> typeValueMap = new HashMap<String, String>();
	private Map<String,String> valueTypeMap = new HashMap<String, String>();
	
	public TypeDictionary(){
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
			if(TypeDictionary.getDefultValueByType(type)==null){
				if(typeValueMap.get(type) == null){
					String value = Integer.toString(index++);
					typeValueMap.put(type, value);
					valueTypeMap.put(value, type);
				}
			}
		}
	}
	
	public String getValueByType(String type){
		String value = TypeDictionary.getDefultValueByType(type);
		if(value==null){
			value = typeValueMap.get(type);
		}
		return value;
	}
	public String getTypeByValue(String value){
		String type = TypeDictionary.getDefultTypeByValue(value);
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
