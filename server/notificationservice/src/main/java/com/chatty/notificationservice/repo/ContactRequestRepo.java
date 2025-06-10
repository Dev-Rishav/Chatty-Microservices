package com.chatty.notificationservice.repo;


import com.chatty.notificationservice.dto.Users;
import com.chatty.notificationservice.entity.ContactRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContactRequestRepo extends JpaRepository<ContactRequest,Long> {

    boolean existsBySenderAndReceiver(Users sender, Users receiver);
    Optional<ContactRequest> findById(long id);

}

