package com.dao;
import com.model.Movie;
import com.dao.DBConn;
import com.dao.daoMovie;
import com.dao.daoFriend;
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
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class daoMovieRecommend {

    /**
     * Return the top 20 popular movies among a user's friends
     *
     * @param email Email of the signed in user
     * @return
     */
    public static ArrayList<Movie> top20_Friend(String email) {
        DBConn db = new DBConn();
        ArrayList<Movie> movie = new ArrayList<>();
        try {
            //1. Get the user's friend list
            ArrayList<String> friend = daoFriend.getFriendList(email);
            if (friend == null || friend.size() == 0) {
                return movie;
            }
            db.connect();
            Connection conn = db.getConn();
            Statement stmt = conn.createStatement();
            //2. Construct sql statement to get the top 20 watched from friends
            String sql = "SELECT r.movieid, count(*) as c, AVG(w.userrating) as a" +
                    " FROM \"RecentlyWatched\" as r, \"WatchesAndRates\" as w WHERE (r.email = '" + friend.get(0)
                    + "'";
            for (int i = 1; i < friend.size(); i++) {
                sql += " OR r.email = '" + friend.get(i) + "'";
            }
            sql += ") AND r.movieid = w.movieid AND r.email = w.email " +
                    "GROUP BY r.movieid, w.movieid ORDER BY c DESC, a DESC LIMIT 20 OFFSET 0;";
            ResultSet rs = stmt.executeQuery(sql);
            ArrayList<Integer> movieid = new ArrayList<>();
            ArrayList<Integer> recommendWatch = new ArrayList<>();
            ArrayList<Long> recommendRating = new ArrayList<>();
            while (rs.next()) {
                movieid.add(rs.getInt("movieid"));
                recommendRating.add(rs.getLong("a"));
                recommendWatch.add(rs.getInt("c"));
            }
            rs.close();
            stmt.close();
            db.disconnect();
            //3. Construct movie list
            movie = daoMovie.constructMovieListByID(movieid);
            for (int i = 0; i < recommendRating.size() || i < recommendWatch.size(); i++) {
                movie.get(i).setRecommendWatched(recommendWatch.get(i));
                movie.get(i).setRecommendRating(recommendRating.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
            db.disconnect();
            movie = null;
        } finally {
            return movie;
        }
    }

    /**
     * Returns the top 20 popular in the past 90 days
     *
     * @return
     */
    public static ArrayList<Movie> top20_90days() {
        ArrayList<Movie> movie = new ArrayList<>();
        DBConn db = new DBConn();
        try {
            db.connect();
            Connection conn = db.getConn();
            Statement stmt = conn.createStatement();
            //1. Get movie id for top 20
            Date today = new Date();
            Calendar cal = new GregorianCalendar();
            cal.add(Calendar.DAY_OF_MONTH, -90);
            Date past90 = cal.getTime();
            String sql = "SELECT r.movieid, count(r.movieid) as c, AVG(w.userrating) as a " +
                    "FROM \"RecentlyWatched\" as r, \"WatchesAndRates\" as w " +
                    "WHERE time <= '" + today + "' AND time >= '" + past90 +
                    "' AND r.movieid = w.movieid AND r.email = w.email" +
                    " GROUP BY r.movieid, w.movieid ORDER BY c DESC, a DESC LIMIT 20 OFFSET 0;";
            ResultSet rs = stmt.executeQuery(sql);
            ArrayList<Integer> movieid = new ArrayList<>();
            ArrayList<Long> recommendRating = new ArrayList<>();
            ArrayList<Integer> recommendWatched = new ArrayList<>();
            while (rs.next()) {
                movieid.add(rs.getInt("movieid"));
                recommendRating.add(rs.getLong("a"));
                recommendWatched.add(rs.getInt("c"));
            }
            rs.close();
            stmt.close();
            db.disconnect();
            //2. Construct movie list
            movie = daoMovie.constructMovieListByID(movieid);
            for (int i = 0; i < recommendRating.size() || i < recommendWatched.size(); i++) {
                movie.get(i).setRecommendWatched(recommendWatched.get(i));
                movie.get(i).setRecommendRating(recommendRating.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
            db.disconnect();
            movie = null;
        } finally {
            return movie;
        }
    }

    /**
     * Returns the top 5 new releases of the month
     *
     * @return
     */
    public static ArrayList<Movie> top5_month() {
        ArrayList<Movie> movie = new ArrayList<>();
        DBConn db = new DBConn();
        try {
            db.connect();
            Connection conn = db.getConn();
            Statement stmt = conn.createStatement();

            // Get the movie id for the top 5 releases of the month
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM");
            LocalDateTime now = LocalDateTime.now();
            String sql = "SELECT w.movieid, count(r.movieid) as c, AVG(w.userrating) as a " +
                    "FROM \"RecentlyWatched\" as r, \"WatchesAndRates\" as w, \"Movies\" as m " +
                    "WHERE w.movieid = m.movieid AND " +
                    "SUBSTRING(m.releaseDate, 1, 7) = '" + dtf.format(now) + "' " +
                    "AND r.movieid = w.movieid AND r.email = w.email" +
                    " GROUP BY r.movieid, w.movieid ORDER BY c DESC, a DESC LIMIT 5 OFFSET 0;";

            ResultSet rs = stmt.executeQuery(sql);
            ArrayList<Integer> movieid = new ArrayList<>();
            ArrayList<Long> recommendRating = new ArrayList<>();
            ArrayList<Integer> recommendWatched = new ArrayList<>();
            while (rs.next()) {
                movieid.add(rs.getInt("movieid"));
                recommendRating.add(rs.getLong("a"));
                recommendWatched.add(rs.getInt("c"));
            }
            rs.close();
            stmt.close();
            db.disconnect();

            // Create the list
            movie = daoMovie.constructMovieListByID(movieid);
            for (int i = 0; i < recommendRating.size() || i < recommendWatched.size(); i++) {
                movie.get(i).setRecommendWatched(recommendWatched.get(i));
                movie.get(i).setRecommendRating(recommendRating.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
            db.disconnect();
            movie = null;
        } finally {
            return movie;
        }
    }

    /**
     * Returns a movie based on their play history and a movie based on the play history of
     * similar users
     * @param email the email of the current user
     * @return
     */
    public static ArrayList<Movie> moviePlayHistory(String email) {
        ArrayList<Movie> movie = new ArrayList<>();
        DBConn db = new DBConn();
        try {
            db.connect();
            Connection conn = db.getConn();
            Statement stmt = conn.createStatement();

            // Get movie id of 5 movies with similar genre to their most recently played
            String sql = "SELECT m.movieid " +
                    "FROM \"Movies\" as m, \"GenreToMovie\" as k " +
                    "WHERE m.movieid = k.\"movieId\" AND k.genre = " +
                    "(SELECT z.genre FROM \"RecentlyWatched\" as r, \"GenreToMovie\" as z " +
                    "WHERE z.\"movieId\" = r.movieid AND r.email = '" + email + "' ORDER BY r.time DESC " +
                    "LIMIT 1) ORDER BY Random() LIMIT 5;";
            ResultSet rs = stmt.executeQuery(sql);
            ArrayList<Integer> movieid = new ArrayList<>();
            while (rs.next()) {
                movieid.add(rs.getInt("movieid"));
            }

            // Get movie id of a movie from a user with similar tastes (similar if they have
            // a similar movie in their collection)
            sql = "SELECT n.movieid FROM \"Movies\" as n, \"MovieCollection\" as y " +
                    "WHERE y.email != '" + email + "' AND n.movieid = " +
                    "(SELECT m.movieid FROM \"Movies\" as m,  \"MovieCollection\" as b, " +
                    "\"Contains\" as c, \"User\" as u WHERE u.email = '" + email + "' AND " +
                    "c.collectionid = b.collectionid AND m.movieid = c.movieid ORDER BY Random() " +
                    "LIMIT 1) LIMIT 1;";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                movieid.add(rs.getInt("movieid"));
            }
            rs.close();
            stmt.close();
            db.disconnect();

            // Create the list of movies
            movie = daoMovie.constructMovieListByID(movieid);

        } catch (Exception e) {
            e.printStackTrace();
            db.disconnect();
            movie = null;
        } finally {
            return movie;
        }
    }
}