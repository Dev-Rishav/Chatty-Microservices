package com.chatty.userservice.repo;


import com.chatty.userservice.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepo extends JpaRepository<Users,Integer> {
    Users findByEmail(String email);
}
