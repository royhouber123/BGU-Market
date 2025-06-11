package support;

import market.application.AppConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(AppConfig.class)
public class AcceptanceTestPersist {
    // No need to redefine beans that are already in AppConfig
    // Only add test-specific bean overrides here if needed
    
    // No need for @Autowired fields in a @Configuration class
    // The beans from AppConfig are automatically available to your tests
}
