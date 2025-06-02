package com.chatty.AuthService.service;


import com.chatty.AuthService.entity.UserPrinciple;
import com.chatty.AuthService.entity.Users;
import com.chatty.AuthService.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository usersRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Users user=usersRepo.findByEmail(email);
        if(user==null){
            System.out.println("user not found!");
            throw new UsernameNotFoundException("user not found");
        }
        return new UserPrinciple(user);
    }

}
