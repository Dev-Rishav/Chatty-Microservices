package com.chatty.AuthService.grpc;


import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

@GrpcService
public class AuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {

    @Override
    public void generateToken(TokenRequest request, StreamObserver<TokenResponse> responseObserver) {
        String token = "mock-jwt-token";

        TokenResponse response = TokenResponse.newBuilder()
                .setToken(token)
                .setValid(true)
                .setEmail(request.getEmail())
                .setMessage("Token generated successfully")
                .setUserId(String.valueOf(request.getUserId()))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void validateToken(TokenRequest request, StreamObserver<TokenResponse> responseObserver) {
        boolean isValid = "mock-jwt-token".equals(request.getToken());

        TokenResponse response = TokenResponse.newBuilder()
                .setValid(isValid)
                .setToken(request.getToken())
                .setMessage(isValid ? "Valid token" : "Invalid token")
                .setEmail(request.getEmail())
                .setUserId(String.valueOf(request.getUserId()))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
