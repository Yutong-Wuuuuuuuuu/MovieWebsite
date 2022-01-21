package com.dao;
import com.model.Movie;
import com.dao.DBConn;
import com.jcraft.jsch.*;

import java.util.HashSet;
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

public class daoMovie{

    public static ArrayList<Movie> getUserTopwatched(String userEmail){
        DBConn db = new DBConn();
        ArrayList<Movie> movie = new ArrayList<>();
        try {
            ArrayList<Integer> movieid = new ArrayList<>();
            db.connect();
            Connection conn = db.getConn();
            Statement stmt = conn.createStatement();
            String sql = "SELECT movieid FROM \"WatchesAndRates\" WHERE email = '" + userEmail + "' ORDER BY watched DESC LIMIT 10;";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                movieid.add(rs.getInt("movieid"));
            }
            rs.close();
            stmt.close();
            db.disconnect();
            movie = constructMovieListByID(movieid);
        } catch (Exception e) {
            db.disconnect();
            e.printStackTrace();
        } finally {
            return movie;
        }
    }

    public static ArrayList<Movie> getUserTopratings(String userEmail){
        DBConn db = new DBConn();
        ArrayList<Movie> movie = new ArrayList<>();
        try {
            ArrayList<Integer> movieid = new ArrayList<>();
            db.connect();
            Connection conn = db.getConn();
            Statement stmt = conn.createStatement();
            String sql = "SELECT movieid FROM \"WatchesAndRates\" WHERE email = '" + userEmail + "' ORDER BY userrating DESC LIMIT 10;";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                movieid.add(rs.getInt("movieid"));
            }
            rs.close();
            stmt.close();
            db.disconnect();
            movie = constructMovieListByID(movieid);
        } catch (Exception e) {
            db.disconnect();
            e.printStackTrace();
        } finally {
            return movie;
        }
    }

    public static ArrayList<Movie> getUserTopratingswatched(String userEmail){
        DBConn db = new DBConn();
        ArrayList<Movie> movie = new ArrayList<>();
        try {
            ArrayList<Integer> movieid = new ArrayList<>();
            db.connect();
            Connection conn = db.getConn();
            Statement stmt = conn.createStatement();
            String sql = "SELECT movieid FROM \"WatchesAndRates\" WHERE email = '" + userEmail + "' ORDER BY userrating, watched DESC LIMIT 10;";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                movieid.add(rs.getInt("movieid"));
            }
            rs.close();
            stmt.close();
            db.disconnect();
            movie = constructMovieListByID(movieid);
        } catch (Exception e) {
            db.disconnect();
            e.printStackTrace();
        } finally {
            return movie;
        }
    }

    /**
     * Get the userrating of a movie
     * @param movieID
     * @return
     */
    public static float getUserRating(Connection conn, int movieID){
        float userRating = -1;
        try{
            Statement stmt = conn.createStatement();
            String sql = "SELECT AVG(userrating) as a FROM \"WatchesAndRates\" WHERE movieid = " + movieID + ";";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()){
                userRating = rs.getLong("a");
            }
            rs.close();
            stmt.close();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            return userRating;
        }

    }

    /**
     * User rates a movie
     * @param email
     * @param movieID
     * @return
     */
    public static boolean rateMovie(String email, int movieID, float rate){
        DBConn db = new DBConn();
        Boolean error = false;
        if(rate > 5 || rate < 0){
            error = true;
            System.out.println("Cannot give a rating for more than 5 stars or less than 0 star!");
            return error;
        }
        try{
            db.connect();
            Connection conn = db.getConn();
            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM \"WatchesAndRates\" WHERE email = '" + email + "' AND movieid = " + movieID + ";";
            ResultSet rs = stmt.executeQuery(sql);
            boolean exist = rs.next();
            if(!exist){
                sql = "INSERT INTO \"WatchesAndRates\" (watched, userrating, email,movieid) VALUES (" +
                        0 + ", " + rate + ", '" + email + "', " + movieID + ");";
                stmt.executeUpdate(sql);
            }else{
                sql = "UPDATE \"WatchesAndRates\" SET userrating = " + rate + " WHERE email = '" + email +
                        "' AND movieid = " + movieID + ";";
                stmt.executeUpdate(sql);
            }
            rs.close();
            stmt.close();
            db.disconnect();
        }catch(Exception e){
            error = true;
            db.disconnect();
            e.printStackTrace();
        }finally{
            return error;
        }

    }

    /**
     * User watch a movie
     * @param movieID
     * @param email
     * @return
     */
    public static boolean watchMovie(ArrayList<Integer> movieID, String email){
        DBConn db = new DBConn();
        Boolean error = false;
        try{
            db.connect();
            Connection conn = db.getConn();
            Statement stmt = conn.createStatement();
            for(int id : movieID){
                String sql = "SELECT * FROM \"WatchesAndRates\" WHERE email = '" + email + "' AND movieid = " + id + ";";
                ResultSet rs = stmt.executeQuery(sql);
                boolean exist = rs.next();
                if(!exist){
                    sql = "INSERT INTO \"WatchesAndRates\" (watched,email,movieid) VALUES (" +
                            1 + ", '" + email + "', " + id + ");";
                    stmt.executeUpdate(sql);
                }else{
                    sql = "UPDATE \"WatchesAndRates\" SET watched = watched + 1 WHERE email = '" + email +
                            "' AND movieid = " + id + ";";
                    stmt.executeUpdate(sql);
                }
                Date date = new Date();
                //Also add it to the Recently Watched table
                sql = "INSERT INTO \"RecentlyWatched\" (movieid,email,time) VALUES (" + id +
                        ", '"+ email + "', '" + date + "');";
                stmt.executeUpdate(sql);
                rs.close();
            }
            stmt.close();
            db.disconnect();
        }catch(Exception e){
            error = true;
            db.disconnect();
            e.printStackTrace();
        }finally{
            return error;
        }

    }

    /**
     * Get all movies without sorting
     * @return list of movies
     */
    public static ArrayList<Movie> getAllMovies(int offset, int limit){
        DBConn db = new DBConn();
        ArrayList<Movie> movie = new ArrayList<>();
        try{
            ArrayList<Integer> movieid = new ArrayList<>();
            db.connect();
            Connection conn = db.getConn();
            Statement stmt = conn.createStatement();
            String sql = "SELECT movieid FROM \"Movies\" OFFSET " + offset + " LIMIT " + limit + ";";
            ResultSet rs = stmt.executeQuery(sql);
            int count = 0;
            while(rs.next() && count < 50){
                movieid.add(rs.getInt("movieid"));
                count++;
            }
            rs.close();
            stmt.close();
            db.disconnect();
            movie = constructMovieListByID(movieid);
        }catch(Exception e){
            e.printStackTrace();
            db.disconnect();
        }finally{
            return movie;
        }
    }

    /**
     * Sort all movies by certain attribute in certain order
     * @param attribute Can only be "title", "studio", "genre", "releasedate"
     * @param order Can only be "ASC", "DESC"
     * @return list of movies
     */
    public static ArrayList<Movie> sortAllBy(String attribute, String order, int offset, int limit){
        attribute = attribute.toLowerCase();
        DBConn db = new DBConn();
        ArrayList<Movie> movie = new ArrayList<>();
        try{
            ArrayList<Integer> movieid = new ArrayList<Integer>();
            db.connect();
            Connection conn = db.getConn();
            Statement stmt = conn.createStatement();
            String sql = "";
            if(attribute.equals("title") || attribute.equals("releasedate")){
                sql = "SELECT movieid FROM \"Movies\" ORDER BY " + attribute + " " + order;
            }else if(attribute.equals("genre")){
                sql = "SELECT m.\"movieId\" FROM \"GenreToMovie\" as m, \"Genre\" as g" +
                        " WHERE g.genreid = m.genre ORDER BY g.genre " + order;
            }else if(attribute.equals("studio")){
                sql = "SELECT P.\"movieId\" FROM \"ProducedBy\" P, \"Contributor\" C " +
                        "WHERE P.\"contributorId\" = C.contributorid ORDER BY C.name " + order;
            }else{
                System.out.println("ERROR: invalid keyword");
                return movie;
            }
            sql += " OFFSET " + offset + " LIMIT " + limit + ";";
            ResultSet rs = stmt.executeQuery(sql);
            int count = 0;
            while(rs.next() && count < 50){
                movieid.add(rs.getInt("movieid"));
                count++;
            }
            rs.close();
            stmt.close();
            db.disconnect();
            movie = constructMovieListByID(movieid);
        }catch(Exception e){
            e.printStackTrace();
            db.disconnect();
        }finally{
            return movie;
        }
    }

    /**
     * Takes in a list of movie ids and return all information of the movies
     * @param movieid
     * @return List of movies
     */
    public static ArrayList<Movie> constructMovieListByID(ArrayList<Integer> movieid){
        ArrayList<Movie> movies = new ArrayList<>();
        try{
            for(int id : movieid){
                Movie m = getMovieByID(id);
                movies.add(m);
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            return movies;
        }
    }

    /**
     * Populate the movies only by name
     * @param movieid
     * @return
     */
    public static ArrayList<Movie> populateName(ArrayList<Integer> movieid){
        DBConn db = new DBConn();
        ArrayList<Movie> movie = new ArrayList<>();
        try{
            db.connect();
            Connection conn = db.getConn();
            Statement stmt = conn.createStatement();
            for(int id : movieid){
                String sql = "SELECT title FROM \"Movies\" WHERE movieid = " + id + ";";
                ResultSet rs = stmt.executeQuery(sql);
                while(rs.next()){
                    String t = rs.getString("title");
                    Movie m = new Movie(t, null, null, null, null, null, id, null, -1);
                    movie.add(m);
                }
                rs.close();
            }

            stmt.close();
            db.disconnect();
        }catch(Exception e){
            e.printStackTrace();
            db.disconnect();
        }finally{
            return movie;
        }
    }

    /***
     * Get a single movie by id
     * @param movieid
     * @return
     */
    public static Movie getMovieByID(int movieid){
        DBConn db = new DBConn();
        Movie movie = null;
        try{
            db.connect();
            Connection conn = db.getConn();
            Statement stmt = conn.createStatement();
            movie = getMovieByID(movieid, conn, stmt);
            db.disconnect();
        }catch(Exception e){
            db.disconnect();
            e.printStackTrace();
        }finally{
            return movie;
        }
    }

    /**
     * Populates a movie object given a movieid
     * @param movieid
     * @return A populated movie object, null if exception
     */
    public static Movie getMovieByID(int movieid, Connection conn, Statement stmt){
        Movie movie = null;
        try{
            String sql = "SELECT * FROM \"Movies\" WHERE movieid = " + movieid + ";";
            ResultSet rs = stmt.executeQuery(sql);
            boolean exist = rs.next();
            if(!exist){
                rs.close();
                movie = null;
                return null;
            }
            String title = rs.getString("title");
            String releaseDate = rs.getString("releasedate");
            int length = rs.getInt("length");
            int budget = rs.getInt("budget");
            String rating = rs.getString("rating");
            rs.close();
            ArrayList<String> castmembers = getArtistFromMovie(movieid, conn);
            ArrayList<String> genre = getGenreFromMovie(movieid, conn);
            String studio = getStudioFromMovie(movieid, conn);
            String director = getDirectorFromMovie(movieid, conn);
            float userrating = getUserRating(conn, movieid);
            //    public Movie(String Title, String Director, String Studio, ArrayList<String> Genre,
            //                 ArrayList<String> CastMembers, int MovieRating, int Movieid)
            movie = new Movie(title, director, studio, genre, castmembers, rating, movieid, releaseDate, userrating);
        }catch(Exception e){
            e.printStackTrace();
            movie = null;
        }finally{
            return movie;
        }
    }

    /**
     * @param contributorid
     * @return list of contributor names
     */
    public static ArrayList<String> getContributorName(Connection conn, ArrayList<Integer> contributorid){
        ArrayList<String> name = new ArrayList<>();
        try{
            Statement stmt = conn.createStatement();
            for(int id : contributorid){
                String sql = "SELECT * FROM \"Contributor\" WHERE contributorid = " + id + ";";
                ResultSet rs = stmt.executeQuery(sql);
                if(rs.next()){
                    name.add(rs.getString("name"));
                }
                rs.close();
            }
            stmt.close();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            return name;
        }
    }

    /**
     * Gets the cast members of a movie given a movie id
     * @param movieid
     * @param conn
     * @return list of contributor names
     */
    public static ArrayList<String> getArtistFromMovie(int movieid, Connection conn){
        ArrayList<String> artist = new ArrayList<>();
        try{
            ArrayList<Integer> artistID = new ArrayList<>();
            Statement stmt = conn.createStatement();
            String sql = "SELECT c.name FROM \"Contributor\" as c, \"ActedBy\" as a WHERE a.movieid = " + movieid +
                    " AND c.contributorid = a.contributorid;";
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                artist.add(rs.getString("name"));
            }
            rs.close();
            stmt.close();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            return artist;
        }
    }

    /**
     * Gets the directors of a movie given a movie id
     * @param movieid
     * @param conn
     * @return list of contributor names
     */
    public static String getDirectorFromMovie(int movieid, Connection conn){
        String director = "";
        try{
            ArrayList<Integer> directorID = new ArrayList<>();
            Statement stmt = conn.createStatement();
            String sql = "SELECT c.name FROM \"Contributor\" as c, \"DirectedBy\" as d WHERE d.movieid = " + movieid +
                    " AND d.contributorid = c.contributorid;";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()){
                director = rs.getString("name");
            }
            rs.close();
            stmt.close();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            return director;
        }
    }

    /**
     * Gets the studio of a movie given a movie id
     * @param movieid
     * @param conn
     * @return list of contributor
     */
    public static String getStudioFromMovie(int movieid, Connection conn){
        String studio = "";
        try{
            ArrayList<Integer> producerID = new ArrayList<>();
            Statement stmt = conn.createStatement();
            String sql = "SELECT c.name FROM \"Contributor\" as c, \"ProducedBy\" as p WHERE p.\"movieId\" = "
                    + movieid + " AND c.contributorid = p.\"contributorId\";";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()){
                studio = rs.getString("name");
            }
            rs.close();
            stmt.close();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            return studio;
        }
    }

    /**
     * Gets the genre of a movie given a movie id
     * @param movieid
     * @param conn
     * @return list of contributor ids
     */
    public static ArrayList<String> getGenreFromMovie(int movieid, Connection conn){
        ArrayList<String> genre = new ArrayList<>();
        try{
            ArrayList<Integer> gid = new ArrayList<>();
            Statement stmt = conn.createStatement();
            String sql = "SELECT g.genre FROM \"GenreToMovie\" as m, \"Genre\" as g WHERE m.\"movieId\" = "
                    + movieid + " AND g.genreid = m.genre;";
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                genre.add(rs.getString("genre"));
            }
            rs.close();
            stmt.close();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            return genre;
        }
    }

    //title, genre, studio, releaseDate, actors
    /**
     * Search by given attributes
     * @param frontend
     * @return List of movie objects
     */
    public static ArrayList<Movie> searchMovie(Movie frontend, int offset, int limit){
        DBConn db = new DBConn();
        ArrayList<Movie> movie = new ArrayList<>();
        try {
            db.connect();
            Connection conn = db.getConn();
            Statement stmt = conn.createStatement();
            HashSet<Integer> set = new HashSet<Integer>();
            boolean first = true;
            //Search by title, have to be exact
            if (frontend.getTitle() != null) {
                first = false;
                String sql = "SELECT movieid FROM \"Movies\" WHERE title = '" + frontend.getTitle() + "';";
                set = populateSet(sql, conn, stmt, "movieid");
                if (set.isEmpty()) {
                    stmt.close();
                    db.disconnect();
                    movie = new ArrayList<>();
                    return movie;
                }
            }
            //Search by genre
            if (frontend.getGenre() != null) {
                ArrayList<String> genre = frontend.getGenre();
                ArrayList<Integer> gid = new ArrayList<>();
                //1. Get the genre id
                String sql = "SELECT genreid FROM \"Genre\" WHERE genre = '" + genre.get(0) + "'";
                for (int i = 1; i < genre.size(); i++) {
                    sql += " OR genre = '" + genre.get(i) + "'";
                }
                sql += ";";
                ResultSet rs = stmt.executeQuery(sql);
                while(rs.next()){
                    gid.add(rs.getInt("genreid"));
                }
                rs.close();
                //2. Get movieid from genre id
                int counter = 65;//this is for generating random alphabets
                String sql2 = " WHERE ";
                sql = "SELECT m.\"movieId\" FROM \"GenreToMovie\" as m INNER JOIN \"GenreToMovie\" as " + (char)counter
                        + " ON m.\"movieId\" = " + (char)counter + ".\"movieId\"";
                sql2 += (char)counter + ".genre = " + gid.get(0);
                counter ++;
                for(int i = 1; i < gid.size(); i++){
                    sql += " JOIN \"GenreToMovie\" as " + (char)counter + " ON m.\"movieId\" = " +
                            (char)counter + ".\"movieId\"";
                    sql2 += " AND " + (char)counter + ".genre = " + gid.get(i);
                    counter ++;
                }
                sql += sql2 + ";";
                if (first) {
                    first = false;
                    set = populateSet(sql, conn, stmt, "movieId");
                } else {
                    rs = stmt.executeQuery(sql);
                    HashSet<Integer> newset = new HashSet<Integer>();
                    while (rs.next()) {
                        int id = rs.getInt("movieId");
                        if (set.contains(id)) {
                            newset.add(id);
                        }
                    }
                    set = newset;
                    rs.close();
                }
                if (set.isEmpty()) {
                    stmt.close();
                    db.disconnect();
                    movie = new ArrayList<>();
                    return movie;
                }
            }
            //Search by studio
            if (frontend.getStudio() != null) {
                //1. get contributor ids first
                int cid = -1;
                String sql = "SELECT contributorid FROM \"Contributor\" WHERE name = '" + frontend.getStudio() + "';";
                ResultSet rs = stmt.executeQuery(sql);
                if(rs.next()){
                    cid = rs.getInt("contributorid");
                }
                rs.close();
                sql = "SELECT \"movieId\" FROM \"ProducedBy\" WHERE \"contributorId\" = '" + cid + "';";
                if (first) {
                    first = false;
                    set = populateSet(sql, conn, stmt, "movieid");
                } else {
                    rs = stmt.executeQuery(sql);
                    HashSet<Integer> newset = new HashSet<Integer>();
                    while (rs.next()) {
                        int id = rs.getInt("movieid");
                        if (set.contains(id)) {
                            newset.add(id);
                        }
                    }
                    set = newset;
                    rs.close();

                }
                if (set.isEmpty()) {
                    stmt.close();
                    db.disconnect();
                    movie = new ArrayList<>();
                    return movie;
                }
            }
            //Search by release date
            if (frontend.getReleasedate() != null) {
                String sql = "SELECT movieid FROM \"Movies\" WHERE releastdate = '" + frontend.getReleasedate() + "';";
                if (first) {
                    first = false;
                    set = populateSet(sql, conn, stmt, "movieid");
                } else {
                    ResultSet rs = stmt.executeQuery(sql);
                    HashSet<Integer> newset = new HashSet<Integer>();
                    while (rs.next()) {
                        int id = rs.getInt("movieid");
                        if (set.contains(id)) {
                            newset.add(id);
                        }
                    }
                    set = newset;
                    rs.close();

                }
                if (set.isEmpty()) {
                    stmt.close();
                    db.disconnect();
                    movie = new ArrayList<>();
                    return movie;
                }
            }
            //Search by cast members TODO
            if (frontend.getCastMemebers() != null) {
                ArrayList<String> actorname = frontend.getCastMemebers();
                //Get actor ids
                String sql = "SELECT contributorid FROM \"Contributor\" WHERE name = '" + actorname.get(0) + "'";
                for(int i = 1; i < actorname.size(); i++){
                    sql += " OR name = '" + actorname.get(i) + "'";
                }
                sql += ";";
                ArrayList<Integer> aid = new ArrayList<>();
                ResultSet rs = stmt.executeQuery(sql);
                while(rs.next()){
                    aid.add(rs.getInt("contributorid"));
                }
                //Construct sql statement to find the intersection
                int counter = 65;
                sql = "SELECT m.movieid FROM \"ActedBy\" as m INNER JOIN \"ActedBy\" as " + (char)counter +
                        " ON m.movieid = " + (char)counter + ".movieid";
                String sql2 = " WHERE " + (char)counter + ".contributorid = " + aid.get(0);
                counter ++;
                for(int i = 1; i < aid.size(); i++){
                    sql += " INNER JOIN \"ActedBy\" as " + (char)counter + " ON m.movieid = " +
                            (char)counter + ".movieid";
                    sql2 += " AND " + (char)counter + ".contributorid = " + aid.get(i);
                    counter ++;
                }
                sql += sql2 + ";";
                if (first) {
                    first = false;
                    set = populateSet(sql, conn, stmt, "movieid");
                } else {
                    rs = stmt.executeQuery(sql);
                    HashSet<Integer> newset = new HashSet<Integer>();
                    while (rs.next()) {
                        int id = rs.getInt("movieid");
                        if (set.contains(id)) {
                            newset.add(id);
                        }
                    }
                    set = newset;
                    rs.close();

                }
                if (set.isEmpty()) {
                    stmt.close();
                    db.disconnect();
                    movie = new ArrayList<>();
                    return movie;
                }

            }
            if(set.isEmpty()){
                stmt.close();
                db.disconnect();
                movie = new ArrayList<>();
                return movie;
            }
            String sql = "SELECT movieid, releasedate, title FROM \"Movies\" WHERE ";
            first = true;
            for(int i : set){
                if(first){
                    first = false;
                    sql += "movieid = " + i;
                }else{
                    sql += " OR movieid = " + i;
                }
            }
            sql += " ORDER BY title, releasedate ASC OFFSET " + offset + " LIMIT " + limit + ";";
            System.out.println(sql);
            ResultSet rs = stmt.executeQuery(sql);
            ArrayList<Integer> lst = new ArrayList<>();
            while(rs.next()){
                lst.add(rs.getInt("movieid"));
            }
            rs.close();
            stmt.close();
            db.disconnect();
            movie = constructMovieListByID(lst);
        }catch(Exception e){
            db.disconnect();
            e.printStackTrace();
        }finally{
            return movie;
        }
    }

    /**
     * Populates movieID given an sql statement and add it to a set
     * @param sql
     * @param conn
     * @param stmt
     * @param movie what movieIds are called in the table
     * @return Hashset of movie ids
     */
    public static HashSet<Integer> populateSet(String sql, Connection conn, Statement stmt, String movie){
        HashSet<Integer> set = new HashSet<Integer>();
        try{
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                set.add(rs.getInt(movie));
            }
            rs.close();
        }catch(Exception e){
            e.printStackTrace();
            set = new HashSet<Integer>();
        }finally{
            return set;
        }

    }
}