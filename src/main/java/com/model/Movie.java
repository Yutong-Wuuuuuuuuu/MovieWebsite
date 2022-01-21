package com.model;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.ArrayList;


public class Movie{

    private String Title;
    private String Director;
    private String Releasedate;
    private String Studio;
    private ArrayList<String> Genre;
    private ArrayList<String> CastMemebers;
    private String MovieRating;
    private int Movieid;
    private float UserRating;
    private int recommendWatched;
    private long recommendRating;


    public Movie(String Title, String Director, String Studio, ArrayList<String> Genre,
                 ArrayList<String> CastMembers, String MovieRating, int Movieid, String ReleaseDate, float UserRating){
        this.Title = Title;
        this.Director = Director;
        this.Releasedate = ReleaseDate;
        this.Studio = Studio;
        this.Genre = Genre;
        this.CastMemebers = CastMembers;
        this.MovieRating = MovieRating;
        this.Movieid = Movieid;
        this.UserRating = UserRating;
        this.recommendRating = -1;
        this.recommendWatched = -1;
    }

    public void setRecommendWatched(int number){
        this.recommendWatched = number;
    }

    public int getRecommendWatched(){
        return this.recommendWatched;
    }

    public void setRecommendRating(long r){
        this.recommendRating = r;
    }

    public long getRecommendRating(){
        return this.recommendRating;
    }

    public String getTitle() {
        return this.Title;
    }

    public String getStudio() {
        return this.Studio;
    }

    public ArrayList<String> getGenre() {
        return this.Genre;
    }

    public String getDirector() {
        return this.Director;
    }

    public String getReleasedate(){
        return this.Releasedate;
    }

    public ArrayList<String> getCastMemebers(){
        return this.CastMemebers;
    }

    public int getMovieid(){
        return this.Movieid;
    }

    public float getUserRating(){
        return this.UserRating;
    }

}
