package cn.zysung.miaosha.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.zysung.miaosha.dao.UserDao;
import cn.zysung.miaosha.po.User;
import cn.zysung.miaosha.service.UserService;
import cn.zysung.miaosha.utils.MD5Util;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

//创建5000个user并生成token
public class UserUtil {
    public static void createUser() throws Exception{
        List<User> users = new ArrayList<User>();
//        //生成用户
//        for(int i=0;i<count;i++) {
//            User user = new User();
//            user.setId(13000000000L+i);
//            user.setLoginCount(1);
//            user.setNickname("user"+i);
//            user.setRegisterDate(new Date());
//            user.setSalt("1a2b3c");
//            user.setPassword(MD5Util.inputPassToDBPass("123456", user.getSalt()));
//            users.add(user);
//        }
//        System.out.println("create user");
		//插入数据库
		Connection conn = DBUtil.getConn();
//		String sql = "insert into user(login_count, nickname, register_date, salt, password, id)values(?,?,?,?,?,?)";
		String sql = "select * from user";
		PreparedStatement pstmt = conn.prepareStatement(sql);
//		for(int i=0;i<users.size();i++) {
//			User user = users.get(i);
//			pstmt.setInt(1, user.getLoginCount());
//			pstmt.setString(2, user.getNickname());
//			pstmt.setTimestamp(3, new Timestamp(user.getRegisterDate().getTime()));
//			pstmt.setString(4, user.getSalt());
//			pstmt.setString(5, user.getPassword());
//			pstmt.setLong(6, user.getId());
//			pstmt.addBatch();
//		}
//		pstmt.executeBatch();
        ResultSet rs = pstmt.executeQuery();
        while(rs.next()){
            User u = new User();
            u.setLoginCount(rs.getInt("login_count"));
            u.setNickname(rs.getString("nickname"));
            u.setRegisterDate(rs.getTimestamp("register_date"));
            u.setSalt(rs.getString("salt"));
            u.setPassword(rs.getString("password"));
            u.setId(rs.getLong("id"));
            users.add(u);
        }
        rs.close();
		pstmt.close();
		conn.close();
//		System.out.println("insert to db");


        //登录，生成token
        String urlString = "http://localhost:8080/login/do_login";
        File file = new File("F:/tokens.txt");
        if(file.exists()) {
            file.delete();
        }
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        file.createNewFile();
        raf.seek(0);
        for(int i=0;i<users.size();i++) {
            User user = users.get(i);
            URL url = new URL(urlString);
            HttpURLConnection co = (HttpURLConnection)url.openConnection();
            co.setRequestMethod("POST");
            co.setDoOutput(true);
            OutputStream out = co.getOutputStream();
            String params = "mobile="+user.getId()+"&password="+MD5Util.inputPassToFormPass("123456");
            out.write(params.getBytes());
            out.flush();
            InputStream inputStream = co.getInputStream();
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte buff[] = new byte[1024];
            int len = 0;
            while((len = inputStream.read(buff)) >= 0) {
                bout.write(buff, 0 ,len);
            }
            inputStream.close();
            bout.close();
            String response = new String(bout.toByteArray());
            JSONObject jo = JSON.parseObject(response);
            String token = jo.getString("data");
            System.out.println("create token : " + user.getId());

            String row = user.getId()+","+token;
            raf.seek(raf.length());
            raf.write(row.getBytes());
            raf.write("\r\n".getBytes());
            System.out.println("write to file : " + user.getId());
        }
        raf.close();

        System.out.println("over");
    }

    public static void main(String[] args)throws Exception {
            createUser();
    }
}

