package cn.zysung.miaosha.utils;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;




public class DBUtil {

//    private static Properties props;

//    static {
//        try {
//            InputStream in = DBUtil.class.getClassLoader().getResourceAsStream("application.properties");
//            props = new Properties();
//            props.load(in);
//            in.close();
//        }catch(Exception e) {
//            e.printStackTrace();
//        }
//    }

    public static Connection getConn() throws Exception{
        String url = "jdbc:mysql://139.199.66.249:3306/miaosha?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";
        String username = "zysung";
        String password = "160058";
        String driver = "com.mysql.cj.jdbc.Driver";
        Class.forName(driver);
        return DriverManager.getConnection(url,username, password);
    }
}