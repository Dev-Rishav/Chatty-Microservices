package com.chatty.notificationservice.controller;



import com.chatty.notificationservice.dto.ContactRequestDTO;
import com.chatty.notificationservice.service.ContactRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/contact")
public class ContactController {


    @Autowired
    private ContactRequestService contactRequestService;


    @MessageMapping("/send-contact-request")
    public String handleContactRequest(ContactRequestDTO request) {
        return contactRequestService.sendContactRequest(request);
    }

    @MessageMapping("/accept-contact-request")
    public String handleAcceptContactRequest(long requestId) {
        return contactRequestService.acceptContactRequest(requestId);
    }

    @MessageMapping("/reject-contact-request")
    public String handleRejectContactRequest(long requestId) {
        return contactRequestService.rejectContactRequest(requestId);
    }
}


