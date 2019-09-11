package com.yolo.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
 
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
     
/**
 * 读取excel文件到List
 */
public class ParserExcelUtil {

    private int totalRows = 0;// 总行数
    private int totalCells = 0;// 总列数
 
    public ParserExcelUtil() {
    }
 
    // 根据文件名读取excel文件
    public List<List<String>> read(String fileName) {
        List<List<String>> dataLst = new ArrayList<List<String>>();
 
        // 检查文件名是否为空或者是否是Excel格式的文件
        if (fileName == null || !fileName.matches("^.+\\.(?i)((xls)|(xlsx))$")) {
            return dataLst;
        }
 
        boolean isExcel2003 = true;
        // 对文件的合法性进行验
        if (fileName.matches("^.+\\.(?i)(xlsx)$")) {
            isExcel2003 = false;
        }
 
        // 检查文件是否存在
        File file = new File(fileName);
        if (file == null || !file.exists()) {
            return dataLst;
        }
 
        try {
            // 读取excel
            dataLst = read(new FileInputStream(file), isExcel2003);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return dataLst;
    }
 
    // 根据流读取Excel文件
    public List<List<String>> read(InputStream inputStream, boolean isExcel2003) {
        List<List<String>> dataLst = null;
        try {
            // 根据版本选择创建Workbook的方式
            Workbook wb = isExcel2003 ? new HSSFWorkbook(inputStream) : new XSSFWorkbook(inputStream);
            dataLst = read(wb);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataLst;
    }
 
    // 得到总行数
    public int getTotalRows() {
        return totalRows;
    }
 
    // 得到总列数
    public int getTotalCells() {
        return totalCells;
    }
 
    // 读取数据
    private List<List<String>> read(Workbook wb) {
        List<List<String>> dataLst = new ArrayList<List<String>>();
 
        Sheet sheet = wb.getSheetAt(0);// 得到第一个shell
        this.totalRows = sheet.getPhysicalNumberOfRows();
        if (this.totalRows >= 1 && sheet.getRow(0) != null) {
            this.totalCells = sheet.getRow(0).getPhysicalNumberOfCells();
        }
 
        // 循环Excel的行
        for (int r = 0; r < this.totalRows; r++) {
            Row row = sheet.getRow(r);
            if (row == null) {
                continue;
            }
 
            ArrayList<String> rowLst = new ArrayList<String>();
 
            // 循环Excel的列
            for (short c = 0; c < this.getTotalCells(); c++) {
                Cell cell = row.getCell(c);
                String cellValue = "";
                if (cell == null) {
                    rowLst.add(cellValue);
                    continue;
                }
 
                // 处理数字型的,自动去零
                cellValue = cell.toString();
                rowLst.add(cellValue);
            }
            dataLst.add(rowLst);
        }
        return dataLst;
    }
 
    
    
    public static String getColValue(ArrayList<String> strList,int columnIndex){
   	 	//String  cell = strList[columnIndex]; //取得第row行第0列的数据
    	
    	int i =0;
    	for(String col:strList){
    		i++;
    		if(i==columnIndex){
    			return col;
    		}
    	}
		return "";
	}
    //////例子**************
    public static void main(String[] args) throws Exception {
 
         //String fileName = "e:\\2012.xls";//读取2003的OK
    	String fileName = "D://zz/aa.xlsx";//读取2003的OK
        //String fileName = "e:\\2013.xlsx";// 读取 2007的OK
        ParserExcelUtil excelUtil = new ParserExcelUtil();
        List<List<String>> dataLst = excelUtil.read(fileName);// 读取excle，并把数据打包成list
 
        System.out.println("    --------------------------excel内容如下--------------------------------");
 
        for (List<String> innerLst : dataLst) {
//            StringBuffer rowData = new StringBuffer();
//            for (String dataStr : innerLst) {
//                rowData.append(",").append(dataStr);
//            }
//            if (rowData.length() > 0) {
//                System.out.println("    " + rowData.deleteCharAt(0).toString());
//            }
        	System.out.println("innerLst=="+innerLst);
        }
 
        System.out.println("    总行数：" + excelUtil.getTotalRows() + " , 总列数：" + excelUtil.getTotalCells());
 
    }
    
}