package com.dao;
import com.model.User;
import com.dao.DBConn;
import com.jcraft.jsch.*;
import com.dao.daoUserLogin;

import java.util.*;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;


/*
Database operations for friend system: search user, follow, unfollow users
 */
    public class daoFriend{
        public static int NumberofFollowers(String email){
            DBConn db = new DBConn();
            int count = 0;
            try {
                db.connect();
                Connection conn = db.getConn();
                Statement stmt = null;
                stmt = conn.createStatement();
                String sql = "SELECT COUNT(*) AS C FROM \"Friends\" WHERE email2 = '" + email + "';";
                ResultSet rs = stmt.executeQuery(sql);
                if(rs.next()) {
                    count = rs.getInt("c");
                }
                rs.close();
                stmt.close();
                db.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                db.disconnect();
            } finally {
                return count;
            }
    }

    public static int NumberofFollowings(String email) {
        DBConn db = new DBConn();
        int count = 0;
        try {
            db.connect();
            Connection conn = db.getConn();
            Statement stmt = null;
            stmt = conn.createStatement();
            String sql = "SELECT COUNT(*) AS C FROM \"Friends\" WHERE email1 = '" + email + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
              count = rs.getInt("c");
            }
            rs.close();
            stmt.close();
            db.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            db.disconnect();
        } finally {
            return count;
        }
    }
    /*
    Search a user by the user's email(blur search)
    */
    public static List<User> SearchUser(String email){
        List<User> userList = new ArrayList<User>();
        DBConn db = new DBConn();
        try{
            db.connect();
            Connection conn = db.getConn();
            Statement stmt = null;
            //1. Do a search on the email
            stmt = conn.createStatement();
            String sql = "SELECT * FROM \"User\" WHERE email LIKE '%" + email + "%';";
            ResultSet rs = stmt.executeQuery(sql);
            //2. Put all matching data into the list and return
            while(rs.next()){
                User user = new User("", rs.getString("email"), rs.getString("name"), rs.getString("birthdate"));
                userList.add(user);
            }
            rs.close();
            stmt.close();
            db.disconnect();
        }catch(Exception e){
            e.printStackTrace();
            db.disconnect();
        }finally{
            return userList;
        }
    }

    /*
    Email1 follows email2
    returns whether there is an error
     */
    public static boolean follow(String email1, String email2){
        boolean err = false;
        DBConn db = new DBConn();
        try{
            boolean followExist = followExist(email1, email2);
            if(followExist){
                err = true;
                return err;
            }
            db.connect();
            Connection conn = db.getConn();
            Statement stmt = null;
            //1. Check whether both users exist
            boolean email1Exist = daoUserLogin.CheckEmailDuplicate(email1, conn);
            boolean email2Exist = daoUserLogin.CheckEmailDuplicate(email2, conn);
            if(!(email1Exist && email2Exist)){
                err = true;
                db.disconnect();
                return err;
            }
            //2. Put data in the friends table
            stmt = conn.createStatement();
            String sql = "INSERT INTO \"Friends\" (email1,email2) VALUES ('" + email1 + "', '" + email2 + "');";
            stmt.executeUpdate(sql);
            stmt.close();
            db.disconnect();
        }catch(Exception e){
            e.printStackTrace();
            db.disconnect();
            err = true;
        }finally{
            return err;
        }
    }

    /*
    Unfollow a friend
     */
    public static boolean unfollow(String email1, String email2){
        boolean err = false;
        DBConn db = new DBConn();
        try {
            //1. Check whether the relationship exists
            boolean exist = followExist(email1, email2);
            if(!exist){
                System.out.println("ERROR: friend relationship does not exist between " + email1 + " and " + email2);
                err = true;
                return err;
            }
            //2. Delete row in table
            db.connect();
            Connection conn = db.getConn();
            Statement stmt = null;
            stmt = conn.createStatement();
            String sql = "DELETE FROM \"Friends\" WHERE email1 = '" + email1 + "' AND email2 = '" + email2 + "';";
            stmt.executeUpdate(sql);
            stmt.close();
            db.disconnect();
        }catch(Exception e){
            e.printStackTrace();
            db.disconnect();
            err = true;
        }finally{
            return err;
        }
    }

    /*
    Find whether the relationship of email1 follows email2 exist
     */
    public static boolean followExist(String email1, String email2){
        boolean exist = false;
        DBConn db = new DBConn();
        try{
            db.connect();
            Connection conn = db.getConn();
            Statement stmt = null;
            stmt = conn.createStatement();
            String sql = "SELECT * FROM \"Friends\" WHERE email1 = '" + email1 + "' AND email2 = '" + email2 + "';";
            ResultSet rs = stmt.executeQuery(sql);
            exist = rs.next();
            rs.close();
            stmt.close();
            db.disconnect();
        }catch(Exception e){
            e.printStackTrace();
            exist = false;
            db.disconnect();
        }finally{
            return exist;
        }
    }

    /*
    Get user by email, input email has to be exact
    return a user object, null if no user found
     */
    public static User getUserByEmail(String email){
        User user = null;
        DBConn db = new DBConn();
        try{
            db.connect();
            Connection conn = db.getConn();
            Statement stmt = null;
            //1. Do a search on the email
            stmt = conn.createStatement();
            String sql = "SELECT * FROM \"User\" WHERE email = '" + email + "';";
            ResultSet rs = stmt.executeQuery(sql);
            //2. Put all matching data into the list and return
            boolean exist = rs.next();
            if(exist){
                user = new User("", rs.getString("email"), rs.getString("name"), rs.getString("birthdate"));
            }
            rs.close();
            stmt.close();
            db.disconnect();
        }catch(Exception e){
            e.printStackTrace();
            user = null;
            db.disconnect();
        }finally{
            return user;
        }
    }

    /*
    Get a user's followings
     */
    public static ArrayList<String> getFriendList(String email){
        ArrayList<String> userList = new ArrayList<String>();
        DBConn db = new DBConn();
        try{
            db.connect();
            Connection conn = db.getConn();
            Statement stmt = null;
            //1. Do a search on the email
            stmt = conn.createStatement();
            String sql = "SELECT * FROM \"Friends\" WHERE email1 = '" + email + "';";
            ResultSet rs = stmt.executeQuery(sql);
            //2. Put all matching data into the list and return
            while(rs.next()){
                String userEmail = rs.getString("email2");
                userList.add(userEmail);
            }
            rs.close();
            stmt.close();
            db.disconnect();
        }catch(Exception e){
            e.printStackTrace();
            userList = null;
            db.disconnect();
        }finally{
            return userList;
        }
    }

}