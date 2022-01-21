package com.dao;
import com.model.MovieCollection;
import com.dao.DBConn;
import com.jcraft.jsch.*;
import com.dao.daoUserLogin;
import com.dao.daoMovie;
import com.model.Movie;

import java.util.ArrayList;
import java.util.*;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;

public class daoMovieCollection{

    public static int NumberofMovieCollections(String email){
        DBConn db = new DBConn();
        int count = 0;
        try{
            db.connect();
            Connection conn = db.getConn();
            Statement stmt = null;
            stmt = conn.createStatement();
            String sql = "SELECT COUNT(*) AS C FROM \"MovieCollection\" WHERE email = '" + email + "';";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()) {
                count = rs.getInt("c");
            }
            rs.close();
            stmt.close();
            db.disconnect();
        }catch(Exception e){
            e.printStackTrace();
            db.disconnect();
        }finally{
            return count;
        }
    }

    /*
    Creates a new movie collection, return the collection ID that is created
     */
    public static int createCollection(String email, String name){
        int CollectionID = -1;
        DBConn db = new DBConn();
        try{
            db.connect();
            Connection conn = db.getConn();
            PreparedStatement stmt = null;
            boolean userExist = daoUserLogin.CheckEmailDuplicate(email, conn);
            if(!userExist){
                System.out.println("ERROR: User does not exist with eamil: " + email);
                db.disconnect();
                return CollectionID;
            }
            String sql = "INSERT INTO \"MovieCollection\" (name,email) VALUES ('" + name + "', '" + email + "')" +
                    "RETURNING collectionid;";
            stmt = conn.prepareStatement(sql);
            stmt.execute();
            ResultSet rs = stmt.getResultSet();
            if(rs.next()){
                CollectionID = rs.getInt("collectionid");
            }
            rs.close();
            stmt.close();
            db.disconnect();
        }catch(Exception e){
            CollectionID = -1;
            db.disconnect();
            e.printStackTrace();
        }finally{
            return CollectionID;
        }
    }

    /*
    Insert a movie into a collection
     */
    public static boolean insertMovie(int collectionID, int movieID){
        boolean err = false;
        DBConn db = new DBConn();
        try{
            db.connect();
            Connection conn = db.getConn();
            Statement stmt = null;
            //1. Check if the movie is already in the collection
            boolean exist = checkMovieInCollection(conn, collectionID, movieID);
            if(exist){
                System.out.println("ERROR: movie " + movieID + " already exists in collection " + collectionID);
                err = true;
                db.disconnect();
                return err;
            }
            //2. Do the insert
            stmt = conn.createStatement();
            String sql = "INSERT INTO \"Contains\" (movieid,collectionid) VALUES (" + movieID + ", " + collectionID + ");";
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
    Remove a movie from the collection
     */
    public static boolean removeMovie(int collectionid, int movieid){
        boolean error = false;
        DBConn db = new DBConn();
        try{
            db.connect();
            Connection conn = db.getConn();
            Statement stmt = null;
            //1. Check if a movie is in a collection
            boolean inCollection = checkMovieInCollection(conn, collectionid, movieid);
            if(!inCollection){
                System.out.println("ERROR: Unable to remove movie " + movieid + " from collection " + collectionid +
                        ": no such movie in collection");
                db.disconnect();
                error = true;
                return error;
            }
            //2. Remove movie
            stmt = conn.createStatement();
            String sql = "DELETE FROM \"Contains\" WHERE movieid = " + movieid +
                    " AND collectionid = " + collectionid +";";
            stmt.executeUpdate(sql);
            stmt.close();
            db.disconnect();
        }catch(Exception e){
            e.printStackTrace();
            db.disconnect();
            error = true;
        }finally{
            return error;
        }
    }

    /*
    Remove the entire collection
     */
    public static boolean removeCollection(int collectionid){
        boolean error = false;
        DBConn db = new DBConn();
        try{
            db.connect();
            Connection conn = db.getConn();
            Statement stmt = null;
            //1. Delete the movies in the Contains table
            stmt = conn.createStatement();
            String sql = "DELETE FROM \"Contains\" WHERE collectionid = " + collectionid + ";";
            stmt.executeUpdate(sql);
            //2. Delete the collection in the MovieCollection table
            sql = "DELETE FROM \"MovieCollection\" WHERE collectionid = " + collectionid + ";";
            stmt.executeUpdate(sql);
            stmt.close();
            db.disconnect();
        }catch(Exception e){
            e.printStackTrace();
            error = true;
            db.disconnect();
        }finally{
            return error;
        }
    }

    /*
    Check if a movie exists in a collection
     */
    private static boolean checkMovieInCollection(Connection conn, int collectionID, int movieID){
        Statement stmt = null;
        boolean exist = false;
        try{
            stmt = conn.createStatement();
            String sql = "SELECT * FROM \"Contains\" WHERE collectionid = " + collectionID + " AND movieid = " + movieID;
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()){
                exist = true;
            }
            rs.close();
            stmt.close();
        }catch(Exception e){
            stmt.close();
            exist = true;
        }finally{
            return exist;
        }
    }

    /*
    Displays a user's movie collection and sort by collection name by ascending order
    Returns a list of movie collections
     */
    public static ArrayList<MovieCollection> getUserMovieCollection(String email){
        ArrayList<MovieCollection> movieCollectionList = new ArrayList<>();
        DBConn db = new DBConn();
        try{
            db.connect();
            Connection conn = db.getConn();
            Statement stmt = null;
            stmt = conn.createStatement();
            String sql = "SELECT * FROM \"MovieCollection\" WHERE email = '" + email + "' ORDER BY name ASC;";
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                int id = rs.getInt("collectionid");
                String name = rs.getString("name");
                MovieCollection movieCollection = getCollection(id, name, conn);
                movieCollectionList.add(movieCollection);
            }
            rs.close();
            stmt.close();
            db.disconnect();
        }catch(Exception e){
            e.printStackTrace();
            db.disconnect();
        }finally{
            return movieCollectionList;
        }
    }

    /**
     * rename a collection
     * @param collectionID
     * @param name new name of the collection
     * @return whether there is an error
     */
    public static boolean renameCollection(int collectionID, String name){
        boolean error = false;
        DBConn db = new DBConn();
        try{
            db.connect();
            Connection conn = db.getConn();
            Statement stmt = conn.createStatement();
            String sql = "UPDATE \"MovieCollection\" SET name = '" + name +
                    "' WHERE collectionid = " + collectionID + ";";
            stmt.executeUpdate(sql);
            stmt.close();
            db.disconnect();
        }catch(Exception e){
            e.printStackTrace();
            error = true;
            db.disconnect();
        }finally{
            return error;
        }
    }

    /**
     * Construct a MovieCollection with all info of the collection
     * @param collectionid
     * @param name name of collection
     * @param conn
     * @return Constructed conection
     */
    public static MovieCollection getCollection(int collectionid, String name, Connection conn){
        MovieCollection movieCollection = null;
        try{
            Statement stmt = null;
            stmt = conn.createStatement();
            //1. get the movie infos in the collection and construct movie objects
            String sql = "SELECT m.length, m.title, m.movieid FROM \"Contains\" as c, " +
                    "\"Movies\" as m WHERE c.collectionid = " + collectionid + " AND m.movieid = c.movieid;";
            ResultSet rs = stmt.executeQuery(sql);
            ArrayList<Movie> m = new ArrayList<>();
            int totalMinutes = 0;
            while(rs.next()){
                totalMinutes += rs.getInt("length");
                //    public Movie(String Title, String Director, String Studio, ArrayList<String> Genre,
                //                 ArrayList<String> CastMembers, String MovieRating, int Movieid, String ReleaseDate, float UserRating)
                Movie mo = new Movie(rs.getString("title"), null, null, null, null, null, rs.getInt("movieid"), null, -1);
                m.add(mo);
            }
            stmt.close();
            //2. Construct the collection
            movieCollection = new MovieCollection(collectionid, m, totalMinutes, name);
        }catch(Exception e){
            e.printStackTrace();
            movieCollection = null;
        }finally{
            return movieCollection;
        }
    }

    /**
     * Get collection's movie ids only
     * @param collectionid
     * @param name name of collection
     * @param conn
     * @return Constructed conection
     */
    public static ArrayList<Integer> getCollctionMovieID(int collectionid) {
        DBConn db = new DBConn();
        ArrayList<Integer> movieid = new ArrayList<>();
        try {
            db.connect();
            Connection conn = db.getConn();
            Statement stmt = null;
            stmt = conn.createStatement();
            //1. get the moviesids in the collection
            String sql = "SELECT movieid FROM \"Contains\" WHERE collectionid = " + collectionid + ";";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                movieid.add(rs.getInt("movieid"));
            }
            stmt.close();
            db.disconnect();
        } catch (Exception e) {
            db.disconnect();
            e.printStackTrace();
        } finally {
            return movieid;
        }
    }

}

