package market;

import market.application.AuthService;
import market.domain.user.IUserRepository;
import market.infrastructure.UserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EntityScan("market.domain")
public class BguMarketApplication {
    
    // Define beans directly in the main application class
    @Bean
    public IUserRepository userRepository() {
        return new UserRepository();
    }
    
    @Bean
    public AuthService authService(IUserRepository userRepository) {
        return new AuthService(userRepository);
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    public static void main(String[] args) {
        SpringApplication.run(BguMarketApplication.class, args);
    }
}
