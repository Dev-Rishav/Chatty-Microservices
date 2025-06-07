package com.chatty.userservice.controller;


import com.chatty.userservice.dto.UserDTO;
import com.chatty.userservice.model.CachedUser;
import com.chatty.userservice.repo.CachedUserRepo;
import com.chatty.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CachedUserRepo cachedUserRepository;

    @GetMapping("/allUsers")
    @ResponseBody
    public List<UserDTO> getAllUsers(){
        return userService.getAllUsers();
    }

    @GetMapping("/searchUsers")
    @ResponseBody
    public List<UserDTO> searchUsers(@RequestParam String query) {
        Iterable<CachedUser> allCachedUsers = cachedUserRepository.findAll();

        return ((List<CachedUser>) allCachedUsers).stream()
                .filter(user -> user.getUsername() != null &&
                        user.getUsername().toLowerCase().contains(query.toLowerCase()))
                .map(user -> new UserDTO( user.getUsername(), user.getEmail(),(int)user.getId()))
                .collect(Collectors.toList());
    }
}

