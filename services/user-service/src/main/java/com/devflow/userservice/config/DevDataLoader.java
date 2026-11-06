package com.devflow.userservice.config;

import com.devflow.userservice.entity.Role;
import com.devflow.userservice.entity.User;
import com.devflow.userservice.repository.UserRepository;
// SLF4J: the standard Java logging API. Logs are better than System.out.println because they
// include timestamps and levels (INFO/WARN/ERROR), and they can be filtered or sent to monitoring tools.
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// CommandLineRunner: an interface Spring calls ONCE, right after the app has fully started.
import org.springframework.boot.CommandLineRunner;
// @Component: "Spring, create one object (a bean) of this class and manage it."
import org.springframework.stereotype.Component;

@Component
public class DevDataLoader implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DevDataLoader.class);
    private final UserRepository userRepository;

    //Constructor Injection (Dependency injection)
    public DevDataLoader(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args){
        String email = "admin@devflow.local";

        if(userRepository.existsByEmail(email)){
            log.info("Admin user already exists, skipping", email);
            return;
        }

        User admin = new User(email, "Not_a_real_hash", "Devflow Admin", Role.ADMIN);

        User saved = userRepository.save(admin);
        log.info("Admin user {} created with id {}", saved.getEmail(), saved.getId());
        log.info("Total users in db: {}", userRepository.count());
    }
}
