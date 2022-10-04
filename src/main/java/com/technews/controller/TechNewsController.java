package com.technews.controller;

import com.technews.model.Post;
import com.technews.model.User;
import com.technews.model.Comment;
import com.technews.model.Vote;
import com.technews.repository.CommentRepository;
import com.technews.repository.PostRepository;
import com.technews.repository.UserRepository;
import com.technews.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class TechNewsController {

    @Autowired
    PostRepository postRepository;

    @Autowired
    VoteRepository voteRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CommentRepository commentRepository;

    @PostMapping("/users/login")
    public String login(@ModelAttribute User user, Model model, HttpServletRequest request) throws Exception {

        // checks whether all the field were properly filled in
        if ((user.getPassword().equals(null) || user.getPassword().isEmpty()) || (user.getEmail().equals(null) || user.getPassword().isEmpty())) {
            model.addAttribute("notice", "Email address and password must be populated in order to login!");
            return "login";
        }

        User sessionUser = userRepository.findUserByEmail(user.getEmail());

        try {
            // If sessionUser is invalid, running .equals() will throw an error
            if (sessionUser.equals(null)) {

            }
            // We will catch an error and notify client that email address is not recognized
        } catch (NullPointerException e) {
            model.addAttribute("notice", "Email address is not recognized!");
            return "login";
        }

        // Validate Password
        String sessionUserPassword = sessionUser.getPassword();
        boolean isPasswordValid = BCrypt.checkpw(user.getPassword(), sessionUserPassword);
        if(isPasswordValid == false) {
            model.addAttribute("notice", "Password is not valid!");
            return "login";
        }

        sessionUser.setLoggedIn(true);
        request.getSession().setAttribute("SESSION_USER", sessionUser);

        return "redirect:/dashboard";
    }

    // add user endpoint
    @PostMapping("/users")
    public String signup(@ModelAttribute User user, Model model, HttpServletRequest request) throws Exception {

        if ((user.getUsername().equals(null) || user.getUsername().isEmpty()) || (user.getPassword().equals(null) || user.getPassword().isEmpty()) || (user.getEmail().equals(null) || user.getPassword().isEmpty())) {
            model.addAttribute("notice", "In order to signup username, email address and password must be populated!");
            return "login";
        }

        try {
            // Encrypt password
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("notice", "Email address is not available! Please choose a different unique email address.");
            return "login";
        }

        User sessionUser = userRepository.findUserByEmail(user.getEmail());

        try {
            if (sessionUser.equals(null)) {

            }
        } catch (NullPointerException e) {
            model.addAttribute("notice", "User is not recognized!");
            return "login";
        }

        sessionUser.setLoggedIn(true);
        request.getSession().setAttribute("SESSION_USER", sessionUser);

        return "redirect:/dashboard";
    }

    // will allow users to add a post from their dashboard
    @PostMapping("/posts")
    public String addPostDashboardPage(@ModelAttribute Post post, Model model, HttpServletRequest request) {

        if ((post.getTitle().equals(null) || post.getTitle().isEmpty()) || (post.getPostUrl().equals(null) || post.getPostUrl().isEmpty())) {
            return "redirect:/dashboardEmptyTitleAndLink";
        }
        // if the session isn't valid, redirect to login.html
        if (request.getSession(false) == null) {
            return "redirect:/login";
        } else {
            User sessionUser = (User) request.getSession().getAttribute("SESSION_USER");
            post.setUserId(sessionUser.getId());
            postRepository.save(post);

            return "redirect:/dashboard";
        }
    }

    @PostMapping("/posts/{id}")
    public String updatePostDashboardPage(@PathVariable int id, @ModelAttribute Post post, Model model, HttpServletRequest request) {

        if (request.getSession(false) == null) {
            model.addAttribute("user", new User());
            return "redirect/dashboard";
        } else {
            Post tempPost = postRepository.getById(id);
            tempPost.setTitle(post.getTitle());
            postRepository.save(tempPost);

            return "redirect:/dashboard";
        }
    }

    // add a new comment
    @PostMapping("/comments")
    public String createCommentCommentsPage(@ModelAttribute Comment comment, Model model, HttpServletRequest request) {
        // if comment text area is blank, throw an exception
        if (comment.getCommentText().isEmpty() || comment.getCommentText().equals(null)) {
            return "redirect:/singlePostEmptyComment/" + comment.getPostId();
        } else {
            if (request.getSession(false) != null) {
                User sessionUser = (User) request.getSession().getAttribute("SESSION_USER");
                comment.setUserId(sessionUser.getId());
                commentRepository.save(comment);
                return "redirect:/post/" + comment.getPostId();
            } else {
                return "login";
            }
        }
    }
    // edit an existing comment
    @PostMapping("/comments/edit")
    public String createCommentEditPage(@ModelAttribute Comment comment, HttpServletRequest request) {

        if (comment.getCommentText().equals("") || comment.getCommentText().equals(null)) {
            return "redirect:/editPostEmptyComment/" + comment.getPostId();
        } else {
            if (request.getSession(false) != null) {
                User sessionUser = (User) request.getSession().getAttribute("SESSION_USER");
                comment.setUserId(sessionUser.getId());
                commentRepository.save(comment);

                return "redirect:/dashboard/edit/" + comment.getPostId();
            } else {
                return "redirect:/login";
            }
        }

    }
    // allow users to upvote posts
    @PutMapping("/posts/upvote")
    public void addVoteCommentsPage(@RequestBody Vote vote, HttpServletRequest request, HttpServletResponse response) {

        if (request.getSession(false) != null) {
            Post returnPost = null;
            User sessionUser = (User) request.getSession().getAttribute("SESSION_USER");
            vote.setUserId(sessionUser.getId());
            voteRepository.save(vote);

            returnPost = postRepository.getById(vote.getPostId());
            returnPost.setVoteCount(voteRepository.countVotesByPostId(vote.getPostId()));
        }
    }
}