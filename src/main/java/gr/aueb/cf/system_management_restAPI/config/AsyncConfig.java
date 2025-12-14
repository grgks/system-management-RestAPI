package gr.aueb.cf.system_management_restAPI.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration      // ← Spring, READS WHEN STARTS
@EnableAsync        //activates async support
public class AsyncConfig {
    // @Async methods will now run in separate threads!
    // This enables asynchronous security event logging
    // without impacting authentication request performance
}

// annotations is enough to spring
//Step 1: User calls logSecurityEvent()
//Step 2: Spring sees @Async
//Step 3: Spring checks: "Is @EnableAsync enabled?"
//Step 4: YES! (from AsyncConfig)
//Step 5: Spring creates new thread from pool
//Step 6: Runs method in that thread
//Step 7: Main thread continues (no waiting!)