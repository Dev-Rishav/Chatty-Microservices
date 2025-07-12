package com.chatty.notificationservice.service;


import com.chatty.notification.grpc.ContactInfo;
import com.chatty.notification.grpc.ContactServiceGrpc;
import com.chatty.notification.grpc.GetUserContactsRequest;
import com.chatty.notification.grpc.GetUserContactsResponse;
import com.chatty.notificationservice.entity.Contact;
import com.chatty.notificationservice.repo.ContactRepo;
import com.chatty.user.grpc.GetUserByEmailRequest;
import com.chatty.user.grpc.UserResponse;
import com.chatty.user.grpc.UserServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.grpc.server.service.GrpcService;

import java.util.List;

@GrpcService
public class GrpcContactServiceImpl extends ContactServiceGrpc.ContactServiceImplBase {


    @Autowired
    private ContactRepo contactRepo;

    @Autowired
    private UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub;

    @Override
    public void getUserContacts(GetUserContactsRequest request, StreamObserver<GetUserContactsResponse> responseObserver) {
        try {
            String userEmail = request.getUserEmail();
            System.out.println("Getting contacts for user: " + userEmail);
            
            // Get all contacts for the user
            List<Contact> contacts = contactRepo.findByOwner(userEmail);
            System.out.println("Found " + contacts.size() + " contacts");
            
            GetUserContactsResponse.Builder responseBuilder = GetUserContactsResponse.newBuilder();
            //! [TODO] Might have to optimize this for AWS, passing list of users on grpc
            for (Contact contact : contacts) {
                // Get contact user details via UserService gRPC
                GetUserByEmailRequest userRequest = GetUserByEmailRequest.newBuilder()
                        .setEmail(contact.getContact())
                        .build();
                
                UserResponse userResponse = userServiceBlockingStub.getUserByEmail(userRequest);
                int contactId=contact.getId().intValue();
                ContactInfo contactInfo = ContactInfo.newBuilder()
                        .setOwnerEmail(userEmail)
                        .setContactEmail(userResponse.getEmail())
                        .setContactId(contactId)
                        .setContactName(userResponse.getUsername())
                        .setProfilePic(userResponse.getProfilePic())
                        .build();
                
                responseBuilder.addContacts(contactInfo);
            }
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            System.err.println("Error getting user contacts: " + e.getMessage());
            e.printStackTrace();
            responseObserver.onError(e);
        }
    }

}
