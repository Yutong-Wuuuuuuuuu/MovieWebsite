package com.dao;

import com.jcraft.jsch.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConn{
    private Connection conn;
    private Session session;

    public DBConn(){
        this.conn = null;
        this.session = null;
    }

    /*
    @return Connection type of the connection
     */
    public Connection getConn(){
        return this.conn;
    }

    /*
    @return Session type of the connection
     */
    public Session getSession(){
        return this.session;
    }

    /*
    Disconnects from the database
     */
    public void disconnect() throws SQLException {
        try{
            if (this.conn != null && !this.conn.isClosed()) {
                //System.out.println("Closing Database Connection");
                this.conn.close();
            }
            if (this.session != null && this.session.isConnected()) {
                //System.out.println("Closing SSH Connection");
                this.session.disconnect();
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    /*
    Connects to the database server
     */
    public void connect() throws SQLException {
        int lport = 5432;
        String rhost = "starbug.cs.rit.edu";
        int rport = 5432;
        String user = ""; //change to your username
        String password = ""; //change to your password
        String databaseName = "p320_33"; //change to your database name

        String driverName = "org.postgresql.Driver";
        this.conn = null;
        this.session = null;
        try {
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            JSch jsch = new JSch();
            this.session = jsch.getSession(user, rhost, 22);
            this.session.setPassword(password);
            this.session.setConfig(config);
            this.session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
            this.session.connect();
            //System.out.println("Connected");
            int assigned_port = this.session.setPortForwardingL(lport, "localhost", rport);
            //System.out.println("Port Forwarded");

            // Assigned port could be different from 5432 but rarely happens
            String url = "jdbc:postgresql://localhost:" + assigned_port + "/" + databaseName;

            //System.out.println("database Url: " + url);
            Properties props = new Properties();
            props.put("user", user);
            props.put("password", password);

            Class.forName(driverName);
            this.conn = DriverManager.getConnection(url, props);
            //System.out.println("Database connection established");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}