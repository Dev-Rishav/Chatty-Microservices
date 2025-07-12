package com.chatty.chatservice.repo;



import com.chatty.chatservice.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepo extends JpaRepository<Chat,Integer> {
    Chat findByUser1IdAndUser2Id(String user1Id, String user2Id);

    @Query("SELECT c.chatId FROM Chat c WHERE c.user1Id = :email OR c.user2Id = :email")
    List<Integer> findChatIdsByUserEmail(@Param("email") String email);


    @Query("""
    SELECT c.chatId FROM Chat c
    WHERE (c.user1Id = :currentUserEmail AND c.user2Id IN :otherUserEmails)
       OR (c.user2Id = :currentUserEmail AND c.user1Id IN :otherUserEmails)
""")
    List<Integer> findChatIdsWithUsers(
            @Param("currentUserEmail") String currentUserEmail,
            @Param("otherUserEmails") List<String> otherUserEmails
    );

}

