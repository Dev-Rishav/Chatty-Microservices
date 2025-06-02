package com.chatty.AuthService.repository;



import com.chatty.AuthService.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Users,Integer> {
    Users findByEmail(String email);
}

