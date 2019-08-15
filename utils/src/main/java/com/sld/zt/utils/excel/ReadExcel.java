package com.sld.zt.utils.excel;

import com.alibaba.fastjson.JSON;
import com.mysql.jdbc.StringUtils;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @program utils
 * @description: 读取excel文件  处理签约记录
 * @author: changhu
 * @create: 2019/05/10 13:00
 */
public class ReadExcel {
    static String insertSql = "INSERT INTO `planet_sign_contract_record`(`user_id`,phone,`renew` ,`hour_type_1` ,`hour_num_1`,`hour_type_2` ,`hour_num_2` ,`hour_type_3` ,`hour_num_3` ,`hour_type_4` ,`hour_num_4`,`sign_date` ,`sign_end_date`,`remark`,`is_renewal` ) VALUES('{user_id}','{phone}','{renew}' ,\"常规课时\" ,'{hour_num_1}',\"STEAM主题课时\" ,'{hour_num_2}' ,\"竞赛集训课时\" ,'{hour_num_3}' ,\"营地课时\" ,'{hour_num_4}','{sign_date}' ,'{sign_end_date}','{remark}','{is_renewal}' );\n";

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
        Workbook userIdWorkBook = getReadWorkBookType("/Users/tz/Desktop/数据清洗导入excel/姓名userId.xlsx");

        try {
            //获取第一个sheet
            Sheet sheet = userIdWorkBook.getSheetAt(0);
            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                String name = getCellStringVal(row.getCell(0)).trim();
                String userId = getCellStringVal(row.getCell(1)).trim().replaceAll("姓名","");
                nameUserIdMap.put(name,userId);
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
                String insertSqlCopy = insertSqlCopy = insertSql;
                String renewSql = insertSql;
                Row row = sheet.getRow(rowNum);
                String name = getCellStringVal(row.getCell(1));
                String userId = nameUserIdMap.get(name);
                if(userId == null){
                    System.out.println("获取不到userId,name:"+name);
                    return null;
                }else {
                    insertSqlCopy = insertSqlCopy.replaceAll("\\{user_id\\}",userId);
                    renewSql = renewSql.replaceAll("\\{user_id\\}",userId);

                }
                String phone = df.format(getCellDoubleVal(row.getCell(4)));
                insertSqlCopy = insertSqlCopy.replaceAll("\\{phone\\}",phone);
                renewSql = renewSql.replaceAll("\\{phone\\}",phone);
                String renew = getCellStringVal(row.getCell(8));
                if(renew ==null || renew.equals("")){
                    continue;
                }
                insertSqlCopy = insertSqlCopy.replaceAll("\\{renew\\}",renew);
                String hour_num_1 = getCellStringVal(row.getCell(17));
                String renew_hour_num_1 = getCellStringVal(row.getCell(19));
                if(hour_num_1 ==null ||hour_num_1.equals("")){
                    insertSqlCopy = insertSqlCopy.replaceAll("\\{hour_num_1\\}",renew_hour_num_1);
                }else{
                    insertSqlCopy = insertSqlCopy.replaceAll("\\{hour_num_1\\}",hour_num_1);
                }
                renewSql = renewSql.replaceAll("\\{hour_num_1\\}",renew_hour_num_1);
                String hour_num_2 = getCellStringVal(row.getCell(27));
                String[] h2Array = hour_num_2.split("/");
                if(h2Array.length==0){
                    insertSqlCopy = insertSqlCopy.replaceAll("\\{hour_num_2\\}","0");
                }else if(h2Array.length==1){
                    insertSqlCopy = insertSqlCopy.replaceAll("\\{hour_num_2\\}",hour_num_2);
                }else {
                    insertSqlCopy = insertSqlCopy.replaceAll("\\{hour_num_2\\}",h2Array[0]);
                }

                renewSql = renewSql.replaceAll("\\{hour_num_2\\}","0");
                String hour_num_3 = getCellStringVal(row.getCell(23));
                String renew_hour_num_3 = getCellStringVal(row.getCell(25));
                if(hour_num_3 ==null ||hour_num_3.equals("")){
                    insertSqlCopy = insertSqlCopy.replaceAll("\\{hour_num_3\\}",renew_hour_num_3);
                }else{
                    insertSqlCopy = insertSqlCopy.replaceAll("\\{hour_num_3\\}",hour_num_3);
                }
                renewSql = renewSql.replaceAll("\\{hour_num_3\\}",renew);
                String hour_num_4 = "0";
                insertSqlCopy = insertSqlCopy.replaceAll("\\{hour_num_4\\}",hour_num_4);
                renewSql = renewSql.replaceAll("\\{hour_num_4\\}",hour_num_4);
                String signDateString = getCellStringVal(row.getCell(7));
                if (signDateString==null || signDateString.trim().equals("")){
                    continue;
                }
                List<Date> dateList = dateFormat(signDateString.trim());
                if(dateList.size()==1){
                    insertSqlCopy = insertSqlCopy.replaceAll("\\{sign_date\\}",simpleDateFormat.format(dateList.get(0)));
                    calendar.setTime(dateList.get(0));
                    calendar.add(Calendar.YEAR,1);
                    insertSqlCopy = insertSqlCopy.replaceAll("\\{sign_end_date\\}",simpleDateFormat.format(calendar.getTime()));

                }else if(dateList.size()==2){
                    insertSqlCopy = insertSqlCopy.replaceAll("\\{sign_date\\}",simpleDateFormat.format(dateList.get(0)));
                    insertSqlCopy = insertSqlCopy.replaceAll("\\{sign_end_date\\}",simpleDateFormat.format(dateList.get(1)));
                }else{
                    insertSqlCopy = insertSqlCopy.replaceAll("\\{sign_date\\}","");
                    insertSqlCopy = insertSqlCopy.replaceAll("\\{sign_end_date\\}","");
                }

                String remark = getCellStringVal(row.getCell(9))+"转介绍："+getCellStringVal(row.getCell(10));
                insertSqlCopy = insertSqlCopy.replaceAll("\\{remark\\}",remark);
                insertSqlCopy = insertSqlCopy.replaceAll("\\{is_renewal\\}","0");
                System.out.println(insertSqlCopy = insertSqlCopy);
                out.write(insertSqlCopy);
                String renewDateString = getCellStringVal(row.getCell(11));

                if(!StringUtils.isNullOrEmpty(renewDateString)){
                    List<Date> renewDateList = dateFormat(signDateString.trim());
                    String renewal = getCellStringVal(row.getCell(12));
                    renewSql = renewSql.replaceAll("\\{renew\\}",renewal);
                    renewSql = renewSql.replaceAll("\\{is_renewal\\}","1");
                    String renewRemark = getCellStringVal(row.getCell(13))+"第一次上课："+getCellStringVal(row.getCell(14))+"起步阶段："+getCellStringVal(row.getCell(15));
                    renewSql = renewSql.replaceAll("\\{remark\\}",renewRemark);
                    if(renewDateList.size()==1){
                        renewSql = renewSql.replaceAll("\\{sign_date\\}",simpleDateFormat.format(renewDateList.get(0)));
                        calendar.setTime(renewDateList.get(0));
                        calendar.add(Calendar.YEAR,2);
                        renewSql = renewSql.replaceAll("\\{sign_end_date\\}",simpleDateFormat.format(calendar.getTime()));

                    }else if(renewDateList.size()==2){
                        renewSql = renewSql.replaceAll("\\{sign_date\\}",simpleDateFormat.format(renewDateList.get(0)));
                        renewSql = renewSql.replaceAll("\\{sign_end_date\\}",simpleDateFormat.format(renewDateList.get(1)));
                    }else{
                        renewSql = renewSql.replaceAll("\\{sign_date\\}","");
                        renewSql = renewSql.replaceAll("\\{sign_end_date\\}","");
                    }
                    out.write(renewSql);


                }




            }
            return contents;
        } finally {
            out.close();
            IOUtils.closeQuietly(workbook);
        }
    }
    static List<Date> dateFormat(String dateString) throws ParseException {
        List<Date> dateList = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        if(StringUtils.isNullOrEmpty(dateString)){
            return dateList;
        }
        /*if(dateString.matches("^[0-9]{4}\\.[0-9]{1,2}\\.[0-9]{1,2}$")){

        }*/
        String[] strArray = dateString.split("-");
        if (strArray.length==1){
            String[]  s = strArray[0].split("\\.");
            if(s[1].length()==1){
                s[1] = "0"+s[1];
            }
            Date signDate = simpleDateFormat.parse(s[0]+s[1]+s[2]);
            dateList.add(signDate);

        }else if(strArray.length==2){
            String[]  s = strArray[0].split("\\.");
            if(s[1].length()==1){
                s[1] = "0"+s[1];
            }
            Date signDate = simpleDateFormat.parse(s[0]+s[1]+s[2]);
            s = strArray[1].split("\\.");
            if(s[1].length()==1){
                s[1] = "0"+s[1];
            }
            Date signEndDate = simpleDateFormat.parse(s[0]+s[1]+s[2]);
            dateList.add(signDate);
            dateList.add(signEndDate);
        }
        return dateList;
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
        List<String> resultList = ReadExcel.readExcel("/Users/tz/Downloads/城西学员信息登记表（190513统计 (2).xls");
        System.out.println(JSON.toJSONString(resultList));






    }
}
