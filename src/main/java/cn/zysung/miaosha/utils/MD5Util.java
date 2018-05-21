package cn.zysung.miaosha.utils;


import org.apache.commons.codec.digest.DigestUtils;
//封装两次MD5的方法
public class MD5Util {
    public static String md5(String src){
        return DigestUtils.md5Hex(src);
    }
    private static final String salt = "1a2b3c4d";
    //第一次MD5
    public static String inputPassToFormPass(String inputPass){
        String str = ""+salt.charAt(0)+salt.charAt(2)+inputPass+salt.charAt(5)+salt.charAt(4);
        return md5(str);
    }
    //第二次MD5
    public static String formPassToDBPass(String formPass,String salt){
        String str = ""+salt.charAt(0)+salt.charAt(2)+formPass+salt.charAt(5)+salt.charAt(4);
        return md5(str);
    }
    //封装整个
    public static String inputPassToDBPass(String input,String saltDB){
        String formPass = inputPassToFormPass(input);
        return formPassToDBPass(formPass,saltDB);
    }


    public static void main(String[] args) {
        System.out.println(inputPassToFormPass("123456")); //d3b1294a61a07da9b49b6e22b2cbd7f9
        System.out.println(inputPassToDBPass("123456","1a2b3c4d"));
    }




}
