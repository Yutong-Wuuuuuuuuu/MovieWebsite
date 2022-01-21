package com.dao;
import com.model.User;
import com.dao.DBConn;
import com.jcraft.jsch.*;

import java.util.*;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

/*
Database operations for user login/register
 */
public class daoUserLogin{

    /*
    Register the user into the system
    @return whether there is an error
     */
    public static boolean UserRegister(User user){
        DBConn db = new DBConn();
        boolean err = false;
        try{
            db.connect();
            Connection conn = db.getConn();
            //1. Check whether the email is already associated with an account
            boolean dup = CheckEmailDuplicate(user.getEmail(), conn);
            if(dup){
                db.disconnect();
                System.out.println("ERROR: Detected duplicate email on email: " + user.getEmail());
                err = true;
                return err;
            }

            //2. Insert into database
            Statement stmt = null;
            Date date = new Date();
            stmt = conn.createStatement();
            String sql = "INSERT INTO \"User\" (email,creationdate,password,name,birthdate) "
                    + "VALUES ('" + user.getEmail() + "', '" + date + "', '" + user.getPassword()
                    + "', '" + user.getName() + "', '" + user.getBirthdate() +  "')";
            stmt.executeUpdate(sql);
            stmt.close();
            db.disconnect();
        }catch(Exception e){
            e.printStackTrace();
            err = true;
            db.disconnect();
        }finally{
            return err;
        }
    }

    /*
    @param user the user to be logged in
    @return Information of the user
     */
    public static User UserLogin(String email, String password){
        User _user = null;
        DBConn db = new DBConn();
        try{
            db.connect();
            Connection conn = db.getConn();
            Statement stmt = null;
            stmt = conn.createStatement();
            String sql = "SELECT * FROM \"User\" WHERE email = '" + email + "';";
            ResultSet rs = stmt.executeQuery(sql);
            //1. Check whether such user exist or
            boolean hasUser = rs.next();
            if(!hasUser || !rs.getString("password").equals(password)){
                System.out.println("ERROR: email or password incorrect" + email);
                rs.close();
                stmt.close();
                db.disconnect();
                return null;
            }
            //2. If passed authentication, build the user
            _user = new User("", rs.getString("email"), "", rs.getString("birthdate"));

            //3, Update access date
            Date date = new Date();
            sql = "INSERT INTO \"UserAcess\" (\"accessDate\",email) VALUES ('" + date + "', '" + email + "');";
            stmt.executeUpdate(sql);
            rs.close();
            stmt.close();
            db.disconnect();
        }catch(Exception e){
            e.printStackTrace();
            db.disconnect();
        }finally{
            return _user;
        }
    }

    /*
    Check whether the email is associated with an account
     */
    public static boolean CheckEmailDuplicate(String email, Connection conn){
        boolean err = false;
        try{
            Statement stmt = null;
            stmt = conn.createStatement();
            String sql = "SELECT * FROM \"User\" WHERE email = '" + email + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()){
                rs.close();
                stmt.close();
                err = true;
                return err;
            }
            rs.close();
            stmt.close();
        }catch(Exception e){
            e.printStackTrace();
            err = true;
        }finally{
            return err;
        }

    }




}