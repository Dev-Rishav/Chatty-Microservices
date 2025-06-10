package com.chatty.notificationservice.repo;




import com.chatty.notificationservice.dto.Users;
import com.chatty.notificationservice.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepo extends JpaRepository<Contact,Long> {
    boolean existsByOwnerAndContact(Users sender, Users receiver);
}

