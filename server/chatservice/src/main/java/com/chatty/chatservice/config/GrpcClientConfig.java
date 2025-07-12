package com.chatty.chatservice.config;




import com.chatty.notification.grpc.ContactServiceGrpc;
import com.chatty.protos.auth.AuthServiceGrpc;
import com.chatty.user.grpc.UserServiceGrpc;
import io.grpc.Channel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class GrpcClientConfig {

    @Bean
    public UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub(GrpcChannelFactory channelFactory) {
        Channel channel = channelFactory.createChannel("user"); // matches "user" in application.properties
        return UserServiceGrpc.newBlockingStub(channel);
    }

    @Bean
    public AuthServiceGrpc.AuthServiceBlockingStub authServiceBlockingStub(GrpcChannelFactory channelFactory) {
        Channel channel = channelFactory.createChannel("auth");
        return AuthServiceGrpc.newBlockingStub(channel);
    }

    @Bean
    public ContactServiceGrpc.ContactServiceBlockingStub contactServiceBlockingStub(GrpcChannelFactory channelFactory) {
        Channel channel = channelFactory.createChannel("contact"); // matches notification service
        return ContactServiceGrpc.newBlockingStub(channel);
    }
}
