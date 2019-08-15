package com.sld.zt.utils.excel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * @program utils
 * @description: 读取excel文件  处理签约记录
 * @author: changhu
 * @create: 2019/05/10 13:00
 */
public class Tuition {
    static String updateSql = "UPDATE `planet_kid` set `tuition` = {tuition} where `parent_id` ={user_id};\n";
    private static Workbook getReadWorkBookType(String filePath) {
        //xls-2003, xlsx-2007
        FileInputStream is = null;

        try {
            is = new FileInputStream(filePath);
            if (filePath.toLowerCase().endsWith("xlsx")) {
                return new XSSFWorkbook(is);
            } else if (filePath.toLowerCase().endsWith("xls")) {
                return new HSSFWorkbook(is);
            } else {
                //  抛出自定义的业务异常
                System.out.println("excel格式文件错误");
                return null;
            }
        } catch (IOException e) {
            //  抛出自定义的业务异常
            e.printStackTrace();
            return null;
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    public static boolean readExcel(String sourceFilePath) throws IOException,ParseException {
        Workbook tuitionWorkBook = getReadWorkBookType(sourceFilePath);


        try {
            //获取第一个sheet
            Sheet sheet = tuitionWorkBook.getSheetAt(0);
            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                String uupdateSqlCopyp = updateSql;
                String tuition = getCellStringVal(row.getCell(0)).trim();
                String userId = getCellStringVal(row.getCell(1)).trim().replaceAll("用户","");
                uupdateSqlCopyp = uupdateSqlCopyp.replaceAll("\\{tuition\\}",tuition);
                uupdateSqlCopyp = uupdateSqlCopyp.replaceAll("\\{user_id\\}",userId);
                System.out.println(uupdateSqlCopyp);
            }

        } finally {
            IOUtils.closeQuietly(tuitionWorkBook);
        }
        return false;
    }


    private static String getCellStringVal(Cell cell) {
        if(cell == null){
            return "";
        }
        CellType cellType = cell.getCellTypeEnum();
        switch (cellType) {
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case STRING:
                return cell.getStringCellValue();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            case ERROR:
                return String.valueOf(cell.getErrorCellValue());
            default:
                return "";
        }
    }
    private static double getCellDoubleVal(Cell cell) {
        if(cell == null){
            return 0;
        }
        CellType cellType = cell.getCellTypeEnum();
        switch (cellType) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                String value = cell.getStringCellValue();
                value = value.replaceAll("[\\u00A0]+", "");
                return Double.valueOf(value);
            default:
                return 0;
        }
    }


    public static void main(String[] args) throws IOException,ParseException {
        //List<String> resultList = ReadExcel.readExcel("/Users/tz/Documents/课时核对.xlsx");
        Tuition.readExcel("/Users/tz/Desktop/数据清洗导入excel/用户学费.xlsx");





    }
}
