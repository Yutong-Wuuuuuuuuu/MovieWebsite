package com.model;
import javax.persistence.*;
import com.dao.daoUserLogin;

public class User{
    private String password;
    private String email; //Unique, primary key
    private String name; //Name to display, need not to be unique
    private String birthdate;


    public User(String password, String email, String name, String birthdate){
        this.password = password;
        this.email = email;
        this.name = name;
        this.birthdate = birthdate;
    }

    public String getEmail(){
        return this.email;
    }

    public String getPassword(){
        return this.password;
    }

    public String getName(){
        return this.name;
    }

    public String getBirthdate(){
        return this.birthdate;
    }

    /*
    Register the user, return whether there is an error
     */
    public boolean register(){
        try{
            boolean err = daoUserLogin.UserRegister(this);
            if(err) return true;
            return false;
        }catch(Exception e){
            e.printStackTrace();
            return true;
        }
    }
    /*
    Register the user, return whether there is an error
     */
    public User login(){
        User info = null;
        try{
            info = daoUserLogin.UserLogin(this.email, this.password);
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            return info;
        }
    }

}