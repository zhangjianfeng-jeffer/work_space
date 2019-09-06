package com.yolo.common.utils.logical;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.yolo.common.utils.StringUtils;

public class LogicalOperationUtils {

	private static Logger logger = LoggerFactory.getLogger(LogicalOperationUtils.class);

	private static final String SYMBOL_AND="&";
	private static final String SYMBOL_OR="|";
	private static final String SYMBOL_INVERTER="!";
	private static final String SYMBOL_BRACKET_LEFT="(";
	private static final String SYMBOL_BRACKET_RIGHT=")";
	
	private static Map<String,Integer> symbolLevel = new HashMap<String, Integer>();
	static{
		symbolLevel.put(SYMBOL_INVERTER, 1);
		symbolLevel.put(SYMBOL_AND, 2);
		symbolLevel.put(SYMBOL_OR, 3);
	}
	
	
	/**
	 * 逻辑表达式计算器
	 * @param expressionItems
	 * @param callbackMap
	 * @return
	 * @throws Exception
	 */
	public static LogicalResult logicalOperation(String expressionItems,Map<String,ILogicalCall> callMap)throws Exception{
		logger.info("逻辑表达式计算器,expressionItems:"+expressionItems);
		List<ExpressionItem> expressionItemList = LogicalOperationUtils.getExpressionItemList(expressionItems);
		ExpressionTree expressionTree = LogicalOperationUtils.getExpressionTree(expressionItemList);
		Boolean check = LogicalOperationUtils.checkExpression(expressionTree,callMap);
		LogicalResult result = null;
		if(Boolean.TRUE.equals(check)){
			result = LogicalOperationUtils.getExpressionResult(expressionTree,callMap);
		}else{
			throw new Exception("逻辑表达式效验,异常！");
		}
		logger.info("逻辑表达式计算器,result:"+JSON.toJSONString(result));
		return result;
	}
	
	/**
	 * 逻辑表达式效验
	 * @param expressions
	 * @param callbackMap
	 * @return
	 * @throws Exception
	 */
	public static Boolean logicalExpressionsCheck(String expressions,Map<String,ILogicalCall> callMap)throws Exception{
		List<ExpressionItem> expressionItemList = LogicalOperationUtils.getExpressionItemList(expressions);
		ExpressionTree expressionTree = LogicalOperationUtils.getExpressionTree(expressionItemList);
		Boolean result = LogicalOperationUtils.checkExpression(expressionTree,callMap);
		return result;
	}
	
	
	public static List<String> getLogicalExpressionsParams(String expressions)throws Exception{
		List<String> paramList = null;
		List<ExpressionItem> expressionItemList = LogicalOperationUtils.getExpressionItemList(expressions);
		if(expressionItemList!=null && expressionItemList.size()>0){
			paramList = new ArrayList<String>();
			for (ExpressionItem expressionItem : expressionItemList) {
				if(ExpressionItem.TYPE_PARAM.equals(expressionItem.getType())){
					paramList.add(expressionItem.getName());
				}
			}
		}
		return paramList;
	}
	
	
	
	private static Boolean checkExpression(ExpressionTree expressionTree,Map<String,ILogicalCall> callMap)throws Exception{
		Boolean result = null;
		if(expressionTree!=null){
			String symbol = expressionTree.getSymbol();
			ExpressionTree left = expressionTree.getLeft();
			ExpressionTree right = expressionTree.getRight();
			if(StringUtils.isNotBlank(symbol)){
				if(LogicalOperationUtils.SYMBOL_AND.equals(symbol) || LogicalOperationUtils.SYMBOL_OR.equals(symbol)){
					if(left != null && right!=null){
						result = LogicalOperationUtils.checkExpression(left,callMap) && LogicalOperationUtils.checkExpression(right,callMap);
					}else{
						logger.info("逻辑表达式效验，运算符参数异常1："+JSON.toJSONString(expressionTree));
					}
				}else if(LogicalOperationUtils.SYMBOL_INVERTER.equals(symbol)){
					if(right!=null){
						result = LogicalOperationUtils.checkExpression(right,callMap);
					}else{
						logger.info("逻辑表达式效验，运算符参数异常2："+JSON.toJSONString(expressionTree));
					}
				}else{
					logger.info("逻辑表达式效验，运算符异常："+JSON.toJSONString(expressionTree));
				}
			}else{
				String paramKey = expressionTree.getParamKey();
				ILogicalCall call= callMap.get(paramKey);
				if(call!=null){
					result = true ;
				}else{
					logger.info("逻辑表达式效验，参数对应的规则方法不存在："+JSON.toJSONString(expressionTree));
				}
			}
		}
		if(result == null){
			logger.info("逻辑表达式效验，异常："+JSON.toJSONString(expressionTree));
			result = false;
		}
		return result;
	}
	
	
	
	
	private static LogicalResult getExpressionResult(ExpressionTree expressionTree,Map<String,ILogicalCall> callMap)throws Exception{
		LogicalResult result = null;
		if(expressionTree!=null){
			String symbol = expressionTree.getSymbol();
			ExpressionTree left = expressionTree.getLeft();
			ExpressionTree right = expressionTree.getRight();
			if(StringUtils.isNotBlank(symbol)){
				if(LogicalOperationUtils.SYMBOL_AND.equals(symbol)){
					result = LogicalOperationUtils.getExpressionResult(left,callMap);
					if(result.isSuccess()){
						result = LogicalOperationUtils.getExpressionResult(right,callMap);
					}
				}else if(LogicalOperationUtils.SYMBOL_OR.equals(symbol)){
					result = LogicalOperationUtils.getExpressionResult(left,callMap);
					if(result.isSuccess()==false){
						result = LogicalOperationUtils.getExpressionResult(right,callMap);
					}
				}else if(LogicalOperationUtils.SYMBOL_INVERTER.equals(symbol)){
					result = LogicalOperationUtils.getExpressionResult(right,callMap);
					if(result.isSuccess()){
						result.setSuccess(false);
					}
				}else{
					logger.info("逻辑表达式计算，运算符异常："+JSON.toJSONString(expressionTree));
				}
			}else{
				String paramKey = expressionTree.getParamKey();
				ILogicalCall call = callMap.get(paramKey);
				if(call!=null){
					result = call.call();
				}
				logger.info("paramKey:"+paramKey+",result:"+JSON.toJSONString(result));
				if(result == null){
					logger.info("逻辑表达式计算，参数对应的规则方法不存在："+JSON.toJSONString(expressionTree));
				}
			}
		}
		if(result == null){
			logger.error("逻辑计算树，运算符异常："+JSON.toJSONString(expressionTree));
			throw new Exception("逻辑运算符异常！");
		}
		return result;
	}
	
	
	private static ExpressionTree getExpressionTree(List<ExpressionItem> expressionItemList){
		List<ExpressionItem> expressionItemNewList = LogicalOperationUtils.groupExpressionItemList(expressionItemList);
		ExpressionTree expressionTree = LogicalOperationUtils.createExpressionTree(expressionItemNewList);
		return expressionTree;
	}
	
	private static ExpressionTree createExpressionTree(List<ExpressionItem> expressionItemNewList){
		ExpressionTree expressionTree = null;
		if(expressionItemNewList!=null && expressionItemNewList.size()>0){
			if(expressionItemNewList.size()==1){
				ExpressionItem item = expressionItemNewList.get(0);
				expressionTree = new ExpressionTree();
				expressionTree.setParamKey(item.getName());
			}else{
				Integer index = LogicalOperationUtils.getMaxRightSymbolIndex(expressionItemNewList);
				if(index!=null){
					expressionTree = new ExpressionTree();
					expressionTree.setSymbol(expressionItemNewList.get(index).getName());
					if(index>0){
						List<ExpressionItem> list = expressionItemNewList.subList(0, index);
						expressionTree.setLeft(LogicalOperationUtils.getNextExpressionTree(list));
					}
					if(index<expressionItemNewList.size()-1){
						List<ExpressionItem> list = expressionItemNewList.subList(index+1, expressionItemNewList.size());
						expressionTree.setRight(LogicalOperationUtils.getNextExpressionTree(list));
					}
				}
			}
		}
		return expressionTree;
	}
	
	private static ExpressionTree getNextExpressionTree(List<ExpressionItem> list){
		ExpressionTree tree = null;
		if(list.size() == 1){
			ExpressionItem item = list.get(0);
			if(StringUtils.isNotBlank(item.getName())){
				tree = new ExpressionTree();
				tree.setParamKey(item.getName());
			}else{
				tree = LogicalOperationUtils.getExpressionTree(item.getExpressionItemList());
			}
		}else{
			tree = LogicalOperationUtils.createExpressionTree(list);
		}
		return tree;
	}
	
	
	private static List<ExpressionItem> groupExpressionItemList(List<ExpressionItem> expressionItemList){
		List<ExpressionItem> result = null;
		if(expressionItemList!=null && expressionItemList.size()>0){
			result = new ArrayList<ExpressionItem>();
			List<ExpressionItem> itemList = null;
			int count = 0;
			for (ExpressionItem expressionItem : expressionItemList) {
				String name = expressionItem.getName();
				if(LogicalOperationUtils.SYMBOL_BRACKET_LEFT.equals(name)){
					if(count==0){
						itemList = new ArrayList<ExpressionItem>();
					}else{
						itemList.add(expressionItem);
					}
					count++;
				}else if(LogicalOperationUtils.SYMBOL_BRACKET_RIGHT.equals(name)){
					count--;
					if(count==0){
						ExpressionItem item = new ExpressionItem();
						item.setExpressionItemList(itemList);
						result.add(item);
					}else{
						itemList.add(expressionItem);
					}
				}else{
					if(count>0){
						itemList.add(expressionItem);
					}else{
						result.add(expressionItem);
					}
				}
			}
		}
		return result;
	}
	
	private static Integer getMaxRightSymbolIndex(List<ExpressionItem> expressionItemList){
		Integer index = null;
		String itemTempName = null;
		if(expressionItemList!=null && expressionItemList.size()>0){
			for (int i = expressionItemList.size()-1; i >=0; i--) {
				ExpressionItem item = expressionItemList.get(i);
				if(isSymbol(item.getName())){
					if(StringUtils.isNotBlank(itemTempName)){
						if(!item.getName().equals(itemTempName)){
							if(symbolLevel.get(item.getName())>symbolLevel.get(itemTempName)){
								itemTempName = item.getName();
								index = i;
							}
						}
					}else{
						itemTempName = item.getName();
						index = i;
					}
				}
			}
		}
		return index;
	}
	

	
	private static List<ExpressionItem> getExpressionItemList(String expressionItems){
		List<ExpressionItem> result = null;
		if(StringUtils.isNotBlank(expressionItems)){
			result = new ArrayList<ExpressionItem>();
			expressionItems = expressionItems.replaceAll(" ", "");
			char[] expressionItemArr = expressionItems.toCharArray();
			String item="";
			for (int i = 0; i < expressionItemArr.length; i++) {
				String express = String.valueOf(expressionItemArr[i]);
				if(isSymbol(express)==true){
					if(StringUtils.isNotBlank(item)){
						ExpressionItem expressionItemFront = new ExpressionItem();
						expressionItemFront.setName(item);
						expressionItemFront.setType(ExpressionItem.TYPE_PARAM);
						result.add(expressionItemFront);
						item = "";
					}
					ExpressionItem expressionItem = new ExpressionItem();
					expressionItem.setName(express);
					expressionItem.setType(ExpressionItem.TYPE_OPERATION);
					result.add(expressionItem);
				}else{
					item = item + express;
					if(i == expressionItemArr.length-1){
						ExpressionItem expressionItemFront = new ExpressionItem();
						expressionItemFront.setName(item);
						expressionItemFront.setType(ExpressionItem.TYPE_PARAM);
						result.add(expressionItemFront);
					}
				}
			}
		}
		return result;
	}
	
	
	private static boolean isSymbol(String expressionItem){
		boolean result = false;
		if(LogicalOperationUtils.SYMBOL_AND.equals(expressionItem) || 
				LogicalOperationUtils.SYMBOL_OR.equals(expressionItem) ||
				LogicalOperationUtils.SYMBOL_INVERTER.equals(expressionItem) ||
				LogicalOperationUtils.SYMBOL_BRACKET_LEFT.equals(expressionItem) ||
				LogicalOperationUtils.SYMBOL_BRACKET_RIGHT.equals(expressionItem) ){
			result = true;
		}
		return result;
	}
	
	public static void main(String[] args) throws Exception{
		final Map<String,ILogicalCall> callbackMap = new HashMap<String,ILogicalCall>();
		ILogicalCall a = new ILogicalCall() {
			public LogicalResult call() throws Exception {
				LogicalResult result = new LogicalResult();
				result.setSuccess(true);
				result.setMessage("aaaaaa");
				return result;
			}
		};
		ILogicalCall b = new ILogicalCall() {
			public LogicalResult call() throws Exception {
				LogicalResult result = new LogicalResult();
				result.setSuccess(true);
				result.setMessage("bbbbbb");
				return result;
			}
		};
		ILogicalCall c = new ILogicalCall() {
			public LogicalResult call() throws Exception {
				LogicalResult result = new LogicalResult();
				result.setSuccess(false);
				result.setMessage("cccccc");
				return result;
			}
		};
		ILogicalCall d = new ILogicalCall() {
			public LogicalResult call() throws Exception {
				LogicalResult result = new LogicalResult();
				result.setSuccess(true);
				result.setMessage("dddddd");
				return result;
			}
		};
		ILogicalCall e = new ILogicalCall() {
			public LogicalResult call() throws Exception {
				LogicalResult result = new LogicalResult();
				result.setSuccess(true);
				result.setMessage("eeeeee");
				return result;
			}
		};
		ILogicalCall f = new ILogicalCall() {
			public LogicalResult call() throws Exception {
				LogicalResult result = new LogicalResult();
				result.setSuccess(true);
				result.setMessage("ffffff");
				return result;
			}
		};
		ILogicalCall g = new ILogicalCall() {
			public LogicalResult call() throws Exception {
				LogicalResult result = new LogicalResult();
				result.setSuccess(true);
				result.setMessage("gggggg");
				return result;
			}
		};
		
		callbackMap.put("a", a);
		callbackMap.put("b", b);
		callbackMap.put("c", c);
		callbackMap.put("d", d);
		callbackMap.put("e", e);
		callbackMap.put("f", f);
		callbackMap.put("g", g);
		
		String expressionItems="!a|b&c&(d&e&!(f&g)|a&(b&c|d)|e)";
		//System.out.println("----------"+LogicalOperationUtil.logicalExpressionsCheck(expressionItems, callbackMap));
		
		
//		List<ExpressionItem> list= LogicalOperationUtil.getExpressionItemList(expressionItems);
//		ExpressionTree expressionTree = getExpressionTree(list);
		//System.out.println(JSON.toJSONString(expressionTree));
		LogicalResult flag=LogicalOperationUtils.logicalOperation(expressionItems, callbackMap);
		System.out.println(JSON.toJSONString(flag));
	}
	
}

class ExpressionItem {
	public static String TYPE_PARAM = Integer.toString(1);
	public static String TYPE_OPERATION = Integer.toString(2);
	
	private String name;
	private String type;
	private List<ExpressionItem> expressionItemList;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<ExpressionItem> getExpressionItemList() {
		return expressionItemList;
	}
	public void setExpressionItemList(List<ExpressionItem> expressionItemList) {
		this.expressionItemList = expressionItemList;
	}
	@Override
	public String toString() {
		return "ExpressionItem [name=" + name + ", type=" + type
				+ ", expressionItemList=" + expressionItemList + "]";
	}
}

class ExpressionTree{
	private String symbol;
	private String paramKey;
	private ExpressionTree left;
	private ExpressionTree right;
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
	public ExpressionTree getLeft() {
		return left;
	}
	public void setLeft(ExpressionTree left) {
		this.left = left;
	}
	public ExpressionTree getRight() {
		return right;
	}
	public void setRight(ExpressionTree right) {
		this.right = right;
	}
	public String getParamKey() {
		return paramKey;
	}
	public void setParamKey(String paramKey) {
		this.paramKey = paramKey;
	}
	
}
