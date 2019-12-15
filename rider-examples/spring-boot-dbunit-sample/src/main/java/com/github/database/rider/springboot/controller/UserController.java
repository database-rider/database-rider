package com.github.database.rider.springboot.controller;

import com.github.database.rider.springboot.model.user.User;
import com.github.database.rider.springboot.model.user.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class UserController {


    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public String create(String email, String name) {
        User user = null;
        try {
            user = new User(email, name);
            userRepository.save(user);
        } catch (Exception ex) {
            return "Error creating the user: " + ex.toString();
        }
        return "User succesfully created! (id = " + user.getId() + ")";
    }

    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @ResponseBody
    public String delete(long id) {
        try {
            User user = new User(id);
            userRepository.delete(user);
        } catch (Exception ex) {
            return "Error deleting the user: " + ex.toString();
        }
        return "User succesfully deleted!";
    }

    @RequestMapping(value = "/get-by-email", method = RequestMethod.GET)
    @ResponseBody
    public String getByEmail(String email) {
        String userId;
        try {
            User user = userRepository.findByEmail(email);
            userId = String.valueOf(user.getId());
        } catch (Exception ex) {
            return "User not found";
        }
        return "The user id is: " + userId;
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public String updateUser(long id, String email, String name) {
        try {
            userRepository.findById(id).ifPresent(
                    (u) -> {
                        u.setEmail(email);
                        u.setName(name);
                        userRepository.save(u);
                    }
            );
        } catch (Exception ex) {
            return "Error updating the user: " + ex.toString();
        }
        return "User succesfully updated!";
    }

    @Autowired
    private UserRepository userRepository;

}
