package com.chatty.userservice.repo;



import com.chatty.userservice.model.CachedUser;
import org.springframework.data.repository.CrudRepository;

public interface CachedUserRepo extends CrudRepository<CachedUser, String> {

}
