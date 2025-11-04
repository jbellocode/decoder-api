package com.decoder.decoder_api.config;
// This class sets up security rules: who can access what in the API.

/* ---- Security + Spring imports explained ---- */
import org.springframework.context.annotation.Bean; // Lets us define reusable components
import org.springframework.context.annotation.Configuration; // Tells Spring this class contains configuration settings
import org.springframework.security.config.annotation.web.builders.HttpSecurity; // Used to set security rules for HTTP requests
import org.springframework.security.core.userdetails.User; // Helps create test users
import org.springframework.security.core.userdetails.UserDetails; // Represents a user in the system
import org.springframework.security.core.userdetails.UserDetailsService; // Service that loads users during login
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Encrypts passwords securely
import org.springframework.security.crypto.password.PasswordEncoder; // Interface for encoding passwords
import org.springframework.security.provisioning.InMemoryUserDetailsManager; // Stores users in memory (for testing only)
import org.springframework.security.web.SecurityFilterChain; // Defines how requests are filtered and secured


/**
 * Configures security for authentication and authorization.
 * Includes temporary test users stored in memory for now.
 */
@Configuration // Spring will load and apply these security rules automatically
public class SecurityConfig {
    
    /**
     * Encrypts passwords using BCrypt — so passwords are not stored in plain text
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    

    /**
     * Creates two test users in memory (Admin + Owner).
     * These users are temporary, just for testing while we build the system
     */
    @Bean
    public UserDetailsService userDetailsService() {

        // Admin — can do everything (including /ingest data)
        UserDetails admin = User.builder()
            .username("admin")
            .password(passwordEncoder().encode("admin123")) // Password is encrypted before storing
            .roles("ADMIN") // Role name used for access checks
            .build();
        
        // Owner — can only access data from buildings they own
        UserDetails owner = User.builder()
            .username("owner")
            .password(passwordEncoder().encode("owner123"))
            .roles("OWNER") // More restricted access
            .build();
        
        // Both users are stored in memory and disappear when app restarts
        return new InMemoryUserDetailsManager(admin, owner);
    }
    

    /**
     * Main security rules for the API endpoints.
     * Blocks unauthorized access and enforces role-based restrictions.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for testing APIs (simple and dev-friendly)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/ingest").hasRole("ADMIN") // Only admins can ingest (save readings)
                .requestMatchers("/buildings/**").authenticated() // Forecast + readings require login
                .requestMatchers("/h2-console/**").permitAll() // Allow database console access during dev
                .anyRequest().permitAll() // Everything else allowed openly for now
            )
            .httpBasic(basic -> {}); // Enable HTTP Basic Authentication (username + password popup)
        
        return http.build(); // Apply rule configuration
    }
}
