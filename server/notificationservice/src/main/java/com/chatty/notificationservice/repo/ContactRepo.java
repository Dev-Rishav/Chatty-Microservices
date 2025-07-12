package com.chatty.notificationservice.repo;




import com.chatty.notificationservice.dto.Users;
import com.chatty.notificationservice.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepo extends JpaRepository<Contact,Long> {
    boolean existsByOwnerAndContact(String senderId, String receiverId);
    
    @Query("SELECT c FROM Contact c WHERE c.owner = :owner")
    List<Contact> findByOwner(@Param("owner") String owner);
}

