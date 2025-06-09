package com.chatty.chatservice.service;

/**
 * The MyUserDetailsService class implements the UserDetailsService interface to load user details by
 * email using a UsersRepo repository.
 */



import com.chatty.chatservice.entity.UserPrinciple;
import com.chatty.chatservice.pojo.Users;
import com.chatty.user.grpc.GetUserByEmailRequest;
import com.chatty.user.grpc.UserResponse;
import com.chatty.user.grpc.UserServiceGrpc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserServiceGrpc.UserServiceBlockingStub userStub;

    @Autowired
    public MyUserDetailsService(UserServiceGrpc.UserServiceBlockingStub userStub) {
        this.userStub = userStub;
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            GetUserByEmailRequest request = GetUserByEmailRequest.newBuilder()
                    .setEmail(email)
                    .build();

            UserResponse response = userStub.getUserByEmail(request);

            if (response == null || response.getEmail().isEmpty()) {
                throw new UsernameNotFoundException("User not found");
            }

            Users user = new Users();
            user.setEmail(response.getEmail());
            user.setUsername(response.getUsername());
            user.setProfilePic(response.getProfilePic());
            user.setUserId(response.getUserId());

            return new UserPrinciple(user);
        } catch (Exception e) {
            throw new UsernameNotFoundException("User not found via gRPC: " + e.getMessage());
        }
    }

}
