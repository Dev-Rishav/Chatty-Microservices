package com.chatty.userservice.service;



import com.chatty.userservice.dto.UserDTO;
import com.chatty.userservice.entity.Users;
import com.chatty.userservice.repo.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {

    @Autowired
    private UsersRepo repo;



    public List<UserDTO> getAllUsers() {

        List<Users>  allUsers =repo.findAll();
        List<UserDTO> allUsersDTO=new ArrayList<>();
        for(int i=0;i<allUsers.size();i++){
            UserDTO userDTO=new UserDTO();
            userDTO.setUsername(allUsers.get(i).getUsername());
            userDTO.setEmail(allUsers.get(i).getEmail());
            userDTO.setUser_id(allUsers.get(i).getUser_id());
            allUsersDTO.add(userDTO);
        }
        return allUsersDTO;
    }


    public Users findByEmail(String receiverEmail) {
        return repo.findByEmail(receiverEmail);
    }
}