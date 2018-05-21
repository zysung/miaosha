package cn.zysung.miaosha.dao;

import cn.zysung.miaosha.po.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserDao {

    @Select("select * from user where id = #{id}")
   public User getById(@Param("id") long id);

    @Update("update user set password  = #{password} where id = #{id]")
    public int update(User tobeUpdate);

    @Select("select * from user")
    public List<User> getUsers();


}
