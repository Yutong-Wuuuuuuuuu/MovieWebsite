package com.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.model.MovieCollection;
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
public class MovieCollectionController{

    @PostMapping("/NumberofMovieCollections")
    public ResponseEntity<Integer> NumberofMovieCollections(@RequestParam String email){
        int num =  daoMovieCollection.NumberofMovieCollections(email);
        return new ResponseEntity<>(num, HttpStatus.OK);
    }

    /**
    Creates new collection
    Takes the user's email and the collection name as param
    Returns the id of the created collection
     */
    @PostMapping("/collection/create")
    public ResponseEntity<Integer> createCollection(@RequestParam String email, @RequestParam String collectionName) {
        int collectionID = daoMovieCollection.createCollection(email, collectionName);
        if(collectionID == -1){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(collectionID, HttpStatus.CREATED);
    }

    /**
    Add a movie to the colleciton
    Pass in 2 params: pathvariable of collectionID; RequestParam of movieID
    Return httpStatus
     */
    @PostMapping("/collection/{collectionID}")
    public ResponseEntity<HttpStatus> insertMovie(@PathVariable("collectionID") int collectionID,
                                                    @RequestParam int movieID) {
        boolean error = daoMovieCollection.insertMovie(collectionID, movieID);
        if(error){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
    Delete a movie from the collction
    Pass in 2 params: pathVariable of collectionID; RequestParam of movieID
    Return httpStatus
    */
    @DeleteMapping("/collection/{collectionID}")
    public ResponseEntity<HttpStatus> removeMovie(@PathVariable("collectionID") int collectionID,
                                               @RequestParam int movieID) {
        boolean err = daoMovieCollection.removeMovie(collectionID, movieID);
        if(err){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
    Delete an entire collection
    Pass in 1 param: pathVariable of collectionID
    Return httpStatus
    */
    @DeleteMapping("/collection/remove/{collectionID}")
    public ResponseEntity<HttpStatus> removeCollection(@PathVariable("collectionID") int collectionID) {
        boolean err = daoMovieCollection.removeCollection(collectionID);
        if(err){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
    Get a list of user's moviecollection
    Pass in 1 param: user's email
    return list of movieCollection
    */
    @GetMapping("/getUserCollection")
    public ResponseEntity<List<MovieCollection>> getUserCollection(@RequestParam String email) {
        List<MovieCollection> _collection = new ArrayList<MovieCollection>();
        _collection = daoMovieCollection.getUserMovieCollection(email);
        if (_collection == null || _collection.size() == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(_collection, HttpStatus.OK);
        }
    }

    /**
    Add a movie to the colleciton
    Pass in 2 params: pathvariable of collectionID; RequestParam of movieID
    Return httpStatus
    */
    @PostMapping("/collection/rename/{collectionID}")
    public ResponseEntity<HttpStatus> insertMovie(@PathVariable("collectionID") int collectionID,
                                                  @RequestParam String name) {
        boolean error = daoMovieCollection.renameCollection(collectionID, name);
        if(error){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}