package com.technews.controller;

import com.technews.model.Post;
import com.technews.model.User;
import com.technews.repository.UserRepository;
import com.technews.repository.VoteRepository;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    UserRepository repository;

    @Autowired
    VoteRepository voteRepository;

    // GET all users
    @GetMapping("/api/users")
    // returns a list of users
    public List<User> getAllUsers() {
        // gets a list of users
        List<User> userList = repository.findAll();
        // for each user in the userList
        for (User u : userList) {
            // get their list of posts
            List<Post> postList = u.getPosts();
            // for each post
            for (Post p : postList) {
                // set the number of votes for each post
                p.setVoteCount(voteRepository.countVotesByPostId(p.getId()));
            }
        }
        return userList;
    }
    // GET a single user
    @GetMapping("/api/users/{id}")
    // returns a single user
    public User getUserById(@PathVariable Integer id) {
        User returnUser = repository.getById(id);
        List<Post> postList = returnUser.getPosts();
        for (Post p : postList) {
            p.setVoteCount(voteRepository.countVotesByPostId(p.getId()));
        }

        return returnUser;
    }

    // POST a user
    @PostMapping("/api/users")
    // @RequestBody annotation maps the body of this request to a transfer object, then deserializes the body onto a Java object
    public User addUser(@RequestBody User user) {
        // Encrypt password
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        repository.save(user);
        return user;
    }
    // PUT (edit) a user
    @PutMapping("/api/users/{id}")
    // @PathVariable allow us to enter id into the request URI as a parameter, replaces /{id}
    public User updateUser(@PathVariable int id, @RequestBody User user) {
        User tempUser = repository.getById(id);

        if (!tempUser.equals(null)) {
            user.setId(tempUser.getId());
            repository.save(user);
        }
        return user;
    }

    @DeleteMapping("/api/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable int id) {
        repository.deleteById(id);
    }
}
