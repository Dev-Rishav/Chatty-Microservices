package com.chatty.userservice.service;



import com.chatty.userservice.entity.Users;
import com.chatty.userservice.model.CachedUser;
import com.chatty.userservice.repo.CachedUserRepo;
import com.chatty.userservice.repo.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserSyncService {

    @Autowired
    private UsersRepo userRepository; // JPA repository

    @Autowired
    private CachedUserRepo redisRepository; // Redis repository

    public void syncUsersToRedis() {
        List<Users> users = userRepository.findAll();

        List<CachedUser> cachedUsers = users.stream().map(user -> {
            CachedUser cached = new CachedUser();
            cached.setId(user.getUser_id());
            cached.setUsername(user.getUsername());
            cached.setEmail(user.getEmail());
            return cached;
        }).toList();

        redisRepository.saveAll(cachedUsers);
        System.out.println("✅ Synced " + cachedUsers.size() + " users to Redis.");
    }
}


