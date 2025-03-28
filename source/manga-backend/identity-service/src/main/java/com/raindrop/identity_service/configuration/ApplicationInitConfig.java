package com.raindrop.identity_service.configuration;

import com.raindrop.identity_service.entity.Role;
import com.raindrop.identity_service.entity.User;
import com.raindrop.identity_service.repository.RoleRepository;
import com.raindrop.identity_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class ApplicationInitConfig {
    PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) {
        return args -> {
            if (!roleRepository.existsByName("ADMIN")) {
                roleRepository.save(Role.builder().name("ADMIN").build());
            }
            if (!userRepository.existsByUsername("admin")) {
                var roles = new HashSet<Role>();
                roles.add(Role.builder().name("ADMIN").build());
                User user = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin"))
                        .roles(roles)
                        .build();
                userRepository.save(user);
                log.warn("Admin user created with password: admin");
            }
        };
    }
}
