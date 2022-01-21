package com.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.model.Movie;
import com.dao.daoMovieRecommend;

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


public class MovieRecommendController{

    /**
     * Get the top 20 movies among user's friends
     * @return
     */
    @GetMapping("/movieRecommend/friends")
    public ResponseEntity<ArrayList<Movie>> RecommendFriends(@RequestParam String email) {
        ArrayList<Movie> movie = daoMovieRecommend.top20_Friend(email);
        if (movie == null || movie.size() == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(movie, HttpStatus.OK);
        }
    }

    /**
     * Get the top 20 movies in the past 90 days
     * @return
     */
    @GetMapping("/movieRecommend/90days")
    public ResponseEntity<ArrayList<Movie>> Recommend90Days() {
        ArrayList<Movie> movie = daoMovieRecommend.top20_90days();
        if (movie == null || movie.size() == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(movie, HttpStatus.OK);
        }
    }

    /**
     * Get the top 5 movies of the past month
     * @return
     */
    @GetMapping("/movieRecommend/month")
    public ResponseEntity<ArrayList<Movie>> RecommendTop5Month() {
        ArrayList<Movie> movie = daoMovieRecommend.top5_month();
        if (movie == null || movie.size() == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(movie, HttpStatus.OK);
        }
    }

    /**
     * Get similar movies
     * @return
     */
    @GetMapping("/movieRecommend/similar")
    public ResponseEntity<ArrayList<Movie>> RecommendSimilar(@RequestParam String email) {
        ArrayList<Movie> movie = daoMovieRecommend.moviePlayHistory(email);
        if (movie == null || movie.size() == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(movie, HttpStatus.OK);
        }
    }
}