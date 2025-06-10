package com.chatty.notificationservice.config;

import com.chatty.protos.auth.AuthServiceGrpc;
import com.chatty.user.grpc.UserServiceGrpc;
import io.grpc.Channel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class GrpcClient {

    @Bean
    public AuthServiceGrpc.AuthServiceBlockingStub authServiceBlockingStub(GrpcChannelFactory channelFactory) {
        Channel channel = channelFactory.createChannel("auth");
        return AuthServiceGrpc.newBlockingStub(channel);
    }

    @Bean
    public UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub(GrpcChannelFactory channelFactory) {
        Channel channel = channelFactory.createChannel("user");
        return UserServiceGrpc.newBlockingStub(channel);
    }
}
