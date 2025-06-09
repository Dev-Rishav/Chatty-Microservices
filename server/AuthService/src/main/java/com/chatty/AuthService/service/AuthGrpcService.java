package com.chatty.AuthService.service;


import com.chatty.AuthService.security.JWTSecurity;
import com.chatty.protos.auth.AuthServiceGrpc;
import com.chatty.protos.auth.TokenRequest;
import com.chatty.protos.auth.TokenResponse;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.grpc.server.service.GrpcService;
import org.springframework.security.core.userdetails.UserDetails;


@GrpcService
public class AuthGrpcService extends AuthServiceGrpc.AuthServiceImplBase {

    @Autowired
    private JWTSecurity jwtService;

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Override
    public void validateToken(TokenRequest request, StreamObserver<TokenResponse> responseObserver) {
        String token = request.getToken();
        String username = jwtService.extractUsername(token);

        TokenResponse.Builder response = TokenResponse.newBuilder().setValid(false);
        if (username != null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtService.validateToken(token, userDetails)) {
                response.setValid(true).setEmail(username);
            }
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }
}


