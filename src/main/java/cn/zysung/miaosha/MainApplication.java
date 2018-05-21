package cn.zysung.miaosha;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MainApplication /*extends SpringBootServletInitializer*/ {

//    @Override  //实现打war包
//    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
//        return builder.sources(MainApplication.class);
//    }

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class,args);
    }

}
