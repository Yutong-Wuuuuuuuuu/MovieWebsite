package com.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.model.User;
import com.dao.daoFriend;

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

public class FriendController{
    /*
    Returns an int of how many people the user is following
     */
    @GetMapping("/NumberofFollowings")
    public ResponseEntity<Integer> NumberofFollowings(@RequestParam String email){
        int Number = daoFriend.NumberofFollowings(email);
        return new ResponseEntity<Integer>(Number, HttpStatus.OK);
    }

    /*
    Returns an int of how many people follow the user
     */
    @GetMapping("/NumberofFollowers")
    public ResponseEntity<Integer> NumberofFollowers(@RequestParam String email){
        int Number = daoFriend.NumberofFollowers(email);
        return new ResponseEntity<Integer>(Number, HttpStatus.OK);
    }

    /*
    Search users, return lists of possible users
     */
    @GetMapping("/searchUsers/{email}")
    public ResponseEntity<List<User>> blurSearchUsers(@PathVariable("email") String email) {
        List<User> _user = new ArrayList<User>();
        _user = daoFriend.SearchUser(email);
        if (_user == null || _user.size() == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(_user, HttpStatus.OK);
        }
    }

    /*
    Get the user's followings
    return list of followings' emails
     */
    @GetMapping("/getFollowings")
    public ResponseEntity<List<String>> getFollowings(@RequestParam String email) {
        List<String> _user = new ArrayList<String>();
        _user = daoFriend.getFriendList(email);
        if (_user == null || _user.size() == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(_user, HttpStatus.OK);
        }
    }

    /*
    Get whether email1 is following email2
    OK = is following
    FORBIDDEN = is not following
    */
    @GetMapping("/isFollowing")
    public ResponseEntity<HttpStatus> isFollowing(@RequestParam String email1, @RequestParam String email2) {
        boolean isFollowing = false;
        isFollowing = daoFriend.followExist(email1, email2);
        if(isFollowing){
            return new ResponseEntity<>(HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    /*
    Follows a user
     */
    @PostMapping("/searchUsers/{email}")
    public ResponseEntity<HttpStatus> follow(@PathVariable("email") String email2, @RequestParam String email1) {
        boolean err = daoFriend.follow(email1, email2);
        if(err){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.CREATED);

    }

    /*
    Unfollows a user
     */
    @DeleteMapping("/searchUsers/{email}")
    public ResponseEntity<HttpStatus> unfollow(@PathVariable("email") String email2, @RequestParam String email1) {
        boolean err = daoFriend.unfollow(email1, email2);
        if(err){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);

    }

}