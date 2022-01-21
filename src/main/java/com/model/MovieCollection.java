package com.model;
import com.model.Movie;
import javax.persistence.*;
import java.util.ArrayList;

public class MovieCollection{
    private int collectionID;
    private ArrayList<Movie> movie;
    private int hour;
    private int minute;
    private String collectionName;
    private int totalMovies;

    public MovieCollection(int collectionID, ArrayList<Movie> movie, int totalMinutes, String collectionName){
        this.collectionID = collectionID;
        this.movie = movie;
        this.hour = totalMinutes / 60;
        this.minute = totalMinutes % 60;
        this.collectionName = collectionName;
        this.totalMovies = movie.size();
    }

    public int getCollectionID(){
        return this.collectionID;
    }

    public ArrayList<Movie> getMovie(){
        return this.movie;
    }

    public String getCollectionName(){
        return this.collectionName;
    }

    public int getHour(){
        return this.hour;
    }

    public int getMinute(){
        return this.minute;
    }
}