package com.sld.zt.utils.excel;

import com.alibaba.fastjson.JSON;
import com.mysql.jdbc.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
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
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @program utils
 * @description: 读取excel文件  处理签约记录滨江校区
 * @author: changhu
 * @create: 2019/05/10 13:00
 */
public class ReadExcelBJ {
    static String insertSql = "INSERT INTO `planet_sign_contract_record`(`user_id`,phone,`renew` ,`hour_type_1` ,`hour_num_1`,`hour_type_2` ,`hour_num_2` ,`hour_type_3` ,`hour_num_3` ,`hour_type_4` ,`hour_num_4`,`sign_date` ,`sign_end_date`,`remark`,`is_renewal`,signatory ) VALUES('{user_id}','{phone}','{renew}' ,\"常规课时\" ,'{hour_num_1}',\"STEAM主题课时\" ,'{hour_num_2}' ,\"竞赛集训课时\" ,'{hour_num_3}' ,\"营地课时\" ,'{hour_num_4}','{sign_date}' ,'{sign_end_date}','{remark}','{is_renewal}','{signatory}');\n";

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

    public static List<String> readExcel(String sourceFilePath) throws IOException,ParseException {
        Workbook workbook = getReadWorkBookType(sourceFilePath);

        BufferedWriter out = new BufferedWriter(new FileWriter("/Users/tz/Desktop/数据清洗导入excel/签约记录.sql"));
        DecimalFormat df = new DecimalFormat("#");
        Calendar calendar = Calendar.getInstance();

        Map<String,String> nameUserIdMap = new HashMap<>(500);
        Map<String,String> namePhoneMap = new HashMap<>(500);
        Workbook userIdWorkBook = getReadWorkBookType("/Users/tz/Desktop/数据清洗导入excel/手机号userId.xlsx");

        try {
            //获取第一个sheet
            Sheet sheet = userIdWorkBook.getSheetAt(0);
            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                String name = getCellStringVal(row.getCell(0)).trim();
                String phone = getCellStringVal(row.getCell(1)).trim().replaceAll("手机号","");
                String userId = getCellStringVal(row.getCell(2)).trim().replaceAll("用户","");
                nameUserIdMap.put(name,userId);
                namePhoneMap.put(name,phone);
            }

        } finally {
            IOUtils.closeQuietly(userIdWorkBook);
        }

        try {
            workbook = getReadWorkBookType(sourceFilePath);
            List<String> contents = new ArrayList<>();

            //获取第一个sheet
            Sheet sheet = workbook.getSheetAt(0);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //第0行是表名，忽略，从第二行开始读取
            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                String insertSqlCopy  = insertSql;
                Row row = sheet.getRow(rowNum);
                String name = getCellStringVal(row.getCell(1));
                String userId = nameUserIdMap.get(name);
                if(userId == null){
                    System.out.println("获取不到userId,name:"+name);
                    return null;
                }else {
                    insertSqlCopy = insertSqlCopy.replaceAll("\\{user_id\\}",userId);
                }
                String phone = namePhoneMap.get(name);
                if(phone == null){
                    System.out.println("获取不到手机号,name:"+name);
                    return null;
                }
                insertSqlCopy = insertSqlCopy.replaceAll("\\{phone\\}",phone);
                String renew = getCellStringVal(row.getCell(5));
                if(renew ==null || renew.equals("")){
                    renew = "0";
                }
                insertSqlCopy = insertSqlCopy.replaceAll("\\{renew\\}",renew);
                String hour_num_1 = getCellStringVal(row.getCell(8));
                if(hour_num_1 ==null ||hour_num_1.equals("")){
                    insertSqlCopy = insertSqlCopy.replaceAll("\\{hour_num_1\\}","0");
                }else{
                    insertSqlCopy = insertSqlCopy.replaceAll("\\{hour_num_1\\}",hour_num_1);
                }
                String hour_num_2 = getCellStringVal(row.getCell(9));
                if(hour_num_2 ==null ||hour_num_2.equals("")){
                    insertSqlCopy = insertSqlCopy.replaceAll("\\{hour_num_2\\}","0");
                }else{
                    insertSqlCopy = insertSqlCopy.replaceAll("\\{hour_num_2\\}",hour_num_2);
                }

                String hour_num_3 = getCellStringVal(row.getCell(10));
                if(hour_num_3 ==null ||hour_num_3.equals("")){
                    insertSqlCopy = insertSqlCopy.replaceAll("\\{hour_num_3\\}","0");
                }else{
                    insertSqlCopy = insertSqlCopy.replaceAll("\\{hour_num_3\\}",hour_num_3);
                }
                String hour_num_4 = "0";
                insertSqlCopy = insertSqlCopy.replaceAll("\\{hour_num_4\\}",hour_num_4);
                //String signDateString = getCellStringVal(row.getCell(3).getDateCellValue());
                String signEndDateString = getCellStringVal(row.getCell(4));
                Date signDate = HSSFDateUtil.getJavaDate(row.getCell(3).getNumericCellValue());
                Date signEndDate = dateFormat(getCellStringVal(row.getCell(4)),"\\.");
                insertSqlCopy = insertSqlCopy.replaceAll("\\{sign_date\\}",simpleDateFormat.format(signDate));
                insertSqlCopy = insertSqlCopy.replaceAll("\\{sign_end_date\\}",simpleDateFormat.format(signEndDate));

                String remark = getCellStringVal(row.getCell(7))+getCellStringVal(row.getCell(11));
                insertSqlCopy = insertSqlCopy.replaceAll("\\{remark\\}",remark);
                String is_renewal = getCellStringVal(row.getCell(6));
                if(is_renewal.equals("续费")){
                    insertSqlCopy = insertSqlCopy.replaceAll("\\{is_renewal\\}","1");
                }else{
                    insertSqlCopy = insertSqlCopy.replaceAll("\\{is_renewal\\}","0");
                }
                String signator = getCellStringVal(row.getCell(2));
                insertSqlCopy = insertSqlCopy.replaceAll("\\{signatory\\}",signator);
                System.out.println(insertSqlCopy);
                out.write(insertSqlCopy);
            }
            return contents;
        } finally {
            out.close();
            IOUtils.closeQuietly(workbook);
        }
    }
    static Date dateFormat(String dateString,String regularString) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        if(StringUtils.isNullOrEmpty(dateString)){
            return null;
        }
        String[]  s = dateString.split(regularString);
        if(s[1].length()==1){
            s[1] = "0"+s[1];
        }
        Date signDate = simpleDateFormat.parse(s[0]+s[1]+s[2]);
        return signDate;
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
        List<String> resultList = ReadExcelBJ.readExcel("/Users/tz/Desktop/数据清洗导入excel/滨江签约信息.xls");
        System.out.println(JSON.toJSONString(resultList));






    }
}
