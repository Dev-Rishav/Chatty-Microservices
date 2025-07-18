package com.chatty.userservice.job;




import com.chatty.userservice.repo.CachedUserRepo;
import com.chatty.userservice.service.UserSyncService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StartupRedisSyncRunner {

    @Autowired
    private CachedUserRepo redisRepo;

    @Autowired
    private UserSyncService syncService;

    @PostConstruct
    public void init() {
        long count = redisRepo.count();
        if (count == 0) {
            System.out.println("🚨 Redis is empty — triggering initial sync...");
            syncService.syncUsersToRedis();
        } else {
            System.out.println("✅ Redis already populated with " + count + " users.");
        }
    }
}



