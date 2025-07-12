package com.rishav.Chatty.repo;

import com.rishav.Chatty.entities.Contact;
import com.rishav.Chatty.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepo extends JpaRepository<Contact,Long> {
    boolean existsByOwnerAndContact(Users sender, Users receiver);
    
    @Query("SELECT c FROM Contact c WHERE c.owner.email = :email")
    List<Contact> findByOwnerEmail(@Param("email") String email);
}
