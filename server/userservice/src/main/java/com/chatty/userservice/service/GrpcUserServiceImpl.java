package com.chatty.userservice.service;


import com.chatty.user.grpc.*;
import com.chatty.userservice.entity.Users;
import com.chatty.userservice.repo.UsersRepo;
import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.grpc.server.service.GrpcService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@GrpcService
public class GrpcUserServiceImpl extends UserServiceGrpc.UserServiceImplBase {


    private UsersRepo usersRepo;

    @Autowired
    public void setUsersRepo(UsersRepo usersRepo) {
        this.usersRepo = usersRepo;
    }

    @Override
    public void getUserByEmail(GetUserByEmailRequest request, StreamObserver<UserResponse> responseObserver) {
        String email = request.getEmail();
        System.out.println("getUserByEmail: " + email);
        Users user = usersRepo.findByEmail(email);
//        System.out.println("getUserByEmail: " + user);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        System.out.println("getUserByEmail: " + user);
        UserResponse res=UserResponse
                .newBuilder()
                .setUserId(user.getUser_id())
                .setEmail(user.getEmail())
                .setCreatedAt(toProtoTimestamp(user.getCreated_at()))
                .setProfilePic(user.getProfilePic()!=null ? user.getProfilePic() : "")
                .build();

        responseObserver.onNext(res);
        responseObserver.onCompleted();
    }

    @Override
    public void getAllUser(Empty request, StreamObserver<AllUserResponse> responseObserver) {
        List<Users> users = usersRepo.findAll();

        List<UserResponse> protoUsers = users.stream()
                .map(user -> UserResponse.newBuilder()
                        .setUserId(user.getUser_id())
                        .setUsername(user.getUsername())
                        .setEmail(user.getEmail())
                        .setProfilePic(user.getProfilePic()!=null ? user.getProfilePic() : "")
                        .setCreatedAt(toProtoTimestamp(user.getCreated_at()))
                        .build()
                )
                .toList();

        AllUserResponse response = AllUserResponse.newBuilder()
                .addAllAllUsers(protoUsers)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    public Timestamp toProtoTimestamp(LocalDateTime localDateTime) {
        Instant instant = localDateTime.toInstant(ZoneOffset.UTC);
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

}
