package de.gameofpods.podcastproject;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import de.gameofpods.podcastproject.config.Config;
import de.gameofpods.podcastproject.data.Role;
import de.gameofpods.podcastproject.data.SamplePersonRepository;
import de.gameofpods.podcastproject.data.User;
import de.gameofpods.podcastproject.data.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.sql.init.SqlDataSourceScriptDatabaseInitializer;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.sql.DataSource;

/**
 * The entry point of the Spring Boot application.
 * <p>
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 */
@SpringBootApplication
@Theme(value = "my-app", variant = Lumo.DARK)
public class Application implements AppShellConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        LOGGER.info("Starting application " + Config.getConfig("application").get("name"));
        SpringApplication.run(Application.class, args);
    }

    @Bean
    SqlDataSourceScriptDatabaseInitializer dataSourceScriptDatabaseInitializer(DataSource dataSource,
                                                                               SqlInitializationProperties properties, SamplePersonRepository repository) {
        // This bean ensures the database is only initialized when empty
        return new SqlDataSourceScriptDatabaseInitializer(dataSource, properties) {
            @Override
            public boolean initializeDatabase() {
                if (repository.count() == 0L) {
                    return super.initializeDatabase();
                }
                return false;
            }
        };
    }

    @Bean
    SqlDataSourceScriptDatabaseInitializer userSourceScriptDatabaseInitializer(
            DataSource dataSource, SqlInitializationProperties properties, UserRepository repository) {
        return new SqlDataSourceScriptDatabaseInitializer(dataSource, properties) {
            @Override
            public boolean initializeDatabase() {
                if (repository.count() == 0L) {
                    super.initializeDatabase();
                    User defaultAdmin = new User();
                    defaultAdmin.setUsername(System.getenv().getOrDefault("DEFAULT_ADMIN", "admin"));
                    var defaultPassword = System.getenv("DEFAULT_PASSWORD");
                    if (defaultPassword == null || defaultPassword.isEmpty()) {
                        LOGGER.error("No default password provided. Please set \"DEFAULT_PASSWORD\" environment");
                        System.exit(1);
                    }
                    defaultAdmin.setHashedPassword(new BCryptPasswordEncoder().encode(defaultPassword));
                    defaultAdmin.setName(defaultAdmin.getUsername());
                    defaultAdmin.setRoles(Role.values());
                    repository.save(defaultAdmin);
                }
                return false;
            }
        };
    }
}
