package org.usermanagement.traceandtrust.config;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.usermanagement.traceandtrust.entity.User;
import org.usermanagement.traceandtrust.enums.Role;
import org.usermanagement.traceandtrust.repository.UserRepository;

/**
 * 🌱 Crée 3 utilisateurs de test au démarrage
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {


    @Bean
    CommandLineRunner initDatabase(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (userRepository.findByEmail("admin@test.com").isEmpty()) {
                User admin = User.builder()
                        .email("admin@test.com")
                        .name("Admin User")
                        .password(passwordEncoder.encode("admin123"))
                        .role(Role.ADMIN)
                        .enabled(true)
                        .accountLocked(false)
                        .build();
                userRepository.save(admin);
                log.info(" Admin : admin@test.com / admin123");
            }

            if (userRepository.findByEmail("manager@test.com").isEmpty()) {
                User manager = User.builder()
                        .email("manager@test.com")
                        .name("Warehouse Manager")
                        .password(passwordEncoder.encode("manager123"))
                        .role(Role.WAREHOUSE_MANAGER)
                        .enabled(true)
                        .accountLocked(false)
                        .build();
                userRepository.save(manager);
                log.info(" Manager : manager@test.com / manager123");
            }

            if (userRepository.findByEmail("client@test.com").isEmpty()) {
                User client = User.builder()
                        .email("client@test.com")
                        .name("Client User")
                        .password(passwordEncoder.encode("client123"))
                        .role(Role.CLIENT)
                        .enabled(true)
                        .accountLocked(false)
                        .build();
                userRepository.save(client);
                log.info(" Client : client@test.com / client123");
            }
        };
    }
}