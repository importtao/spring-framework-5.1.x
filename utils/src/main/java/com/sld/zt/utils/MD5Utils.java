package com.sld.zt.utils;

        import java.io.UnsupportedEncodingException;
        import java.security.MessageDigest;
        import java.security.NoSuchAlgorithmException;


public class MD5Utils {

    /***
     * MD5加码 生成32位md5码
     */
    public static String string2MD5(String inStr){
        MessageDigest md5 = null;
        try{
            md5 = MessageDigest.getInstance("MD5");
        }catch (Exception e){
            System.out.println(e.toString());
            e.printStackTrace();
            return "";
        }
        char[] charArray = inStr.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for (int i = 0; i < charArray.length; i++)
            byteArray[i] = (byte) charArray[i];
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++){
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16)
                hexValue.append("0");
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();

    }

    /**
     * 加密解密算法 执行一次加密，两次解密
     */
    public static String convertMD5(String inStr){

        char[] a = inStr.toCharArray();
        for (int i = 0; i < a.length; i++){
            a[i] = (char) (a[i] ^ 't');
        }
        String s = new String(a);
        return s;

    }

    // 测试主函数
    public static void main(String args[]) {
        //String s = new String("ed79646cf2a0a7b7562eff1a4d84cf7e");
        String s = new String("1234");
        System.out.println("原始：" + s);
        System.out.println("MD5后：" + string2MD5(s));
        System.out.println("加密的：" + convertMD5(s));
        System.out.println("解密的：" + convertMD5(convertMD5(s)));

        for(int i=1;i<=50;i++){
            System.out.println("insert into `planet_line_class_course`(`class_id`,`course_id`,`section_id`,`class_hour`,`section_name`,`section_custom_time`,`section_time`,`section_end_time`,`course_type`,`remark`,`deleted`,`is_call`,`create_time`,`substitute`,`substitute_teacher`,`is_free`,`order_index`,`hour_type`,`seller_id`) values(473,1,1,1,'第"+i+"节','周二','2018-02-06 00:00:00','2018-02-06 01:00:00',1,null,0,0,'2019-05-14 20:11:40',0,null,0,"+i+",1,1);\n");
        }

    }
}