package com.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.model.Movie;
import com.dao.daoMovie;
import com.dao.daoMovieCollection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api")
public class MovieController{

    @GetMapping("/getUserTopratings")
    public ResponseEntity<List<Movie>>getUserTopratings(@RequestParam String email){
        List<Movie> movies = new ArrayList<Movie>();
        movies = daoMovie.getUserTopratings(email);
        if (movies == null || movies.size() == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(movies, HttpStatus.OK);
        }
    }

    @GetMapping("/getUserTopratingswatched")
    public ResponseEntity<List<Movie>>getUserTopratingswatched(@RequestParam String email){
        List<Movie> movies = new ArrayList<Movie>();
        movies = daoMovie.getUserTopratingswatched(email);
        if (movies == null || movies.size() == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(movies, HttpStatus.OK);
        }
    }

    @GetMapping("/getUserTopwatched")
    public ResponseEntity<List<Movie>>getUserTopwatched(@RequestParam String email){
        List<Movie> movies = new ArrayList<Movie>();
        movies = daoMovie.getUserTopwatched(email);
        if (movies == null || movies.size() == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(movies, HttpStatus.OK);
        }
    }

    /***
     * User can rate a movie
     * @param movieID
     * @param rate
     * @param email
     * @return
     */
    @PostMapping("/movie/rate")
    public ResponseEntity<HttpStatus> rateMovie(@RequestParam int movieID,
                                                 @RequestParam int rate,
                                                 @RequestParam String email){
        boolean error = false;
        if(rate > 5 || rate < 0){
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        error = daoMovie.rateMovie(email, movieID, rate);
        if(error){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }else{
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }


    /**
     * User either watch a movie or a collection, pass in a collectionID or movieID and the user's email
     * For the empty one, enter the id as -1. If none are passed in or both are passed in, the function would
     * not work.
     * @param collectionID collectionID, if not using enter -1
     * @param movieID movieID, if not using enter -1
     * @param email User's email
     * @return httpstatus
     */
    @PostMapping("/movie/watch")
    public ResponseEntity<HttpStatus> watchMovie(@RequestParam int collectionID,
                                                  @RequestParam int movieID,
                                                  @RequestParam String email){
        boolean error = false;
        if(collectionID == -1 && movieID != -1){
            ArrayList<Integer> movie = new ArrayList<>();
            movie.add(movieID);
            error = daoMovie.watchMovie(movie, email);
        }else if(collectionID != -1 && movieID == -1){
            ArrayList<Integer> movies = new ArrayList<>();
            movies = daoMovieCollection.getCollctionMovieID(collectionID);
            error = daoMovie.watchMovie(movies, email);
        }else{
            error = true;
        }
        if(error){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }else{
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }

    /**
     * Get one movie by id
     * @param movieid
     * @return
     */
    @GetMapping("/movie/{movieid}")
    public ResponseEntity<Movie> getMovieByID(@PathVariable("movieid") int movieid) {
        Movie movie = daoMovie.getMovieByID(movieid);
        if (movie == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(movie, HttpStatus.OK);
        }
    }

    /**
     * Get a list of movies by a list of ids
     * NOTE: the requestparams are not required:
     * 1. when none are passed, it will simply get a list of all the movie without sorting
     * 2. attribute can only be "title", "studio", "genre", "releasedate"
     * 3. order can only be "ASC", "DESC"
     * 4. Refer to daoMovie.sortAllBy and daoMovie.getAllMovies
     * 5. If you need to pass in requestparams, you MUST pass in both to work, otherwise it will just
     * return the unsorted list
     * @return
     */
    @GetMapping("/movie")
    public ResponseEntity<List<Movie>> getAllMovies(@RequestParam(required = false) String attribute,
                                                    @RequestParam(required = false) String order,
                                                    @RequestParam int offset,
                                                    @RequestParam int limit) {
        try {
            List<Movie> movie = new ArrayList<>();
            if (attribute == null || order == null){
                movie = daoMovie.getAllMovies(offset, limit);
            }else{
                movie = daoMovie.sortAllBy(attribute, order, offset, limit);
            }
            if(movie == null || movie.size() == 0){
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(movie, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /***
     * Search for a list of movies based on attributes
     * @param movieInput A Movie object, user can search through title, genre, studio, release date, and actors
     * @return List of movies
     */
    @PostMapping("/searchMovie")
    public ResponseEntity<List<Movie>> searchMovie(@RequestBody Movie movieInput, @RequestParam int offset,
                                                   @RequestParam int limit) {
        try {
            List<Movie> movie = new ArrayList<>();
            movie = daoMovie.searchMovie(movieInput, offset, limit);
            if(movie == null || movie.size() == 0){
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(movie, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}