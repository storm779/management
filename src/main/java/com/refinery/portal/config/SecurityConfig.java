package com.refinery.portal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.refinery.portal.service.UserService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, @Lazy UserService userService) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // Public pages - accessible without authentication
                .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                // API endpoints for registration validation
                .requestMatchers("/api/check-username", "/api/check-email").permitAll()
                // What's New viewing is public, but management requires authentication
                .requestMatchers("/whatsnew/list").permitAll()
                .requestMatchers("/whatsnew/view/**").permitAll()
                // Message Board viewing is public, API endpoints are public
                .requestMatchers("/messageboard/list").permitAll()
                .requestMatchers("/messageboard/view/**").permitAll()
                .requestMatchers("/messageboard/api/**").permitAll()
                // Protected admin functions - require ADMIN role
                .requestMatchers("/whatsnew/new", "/whatsnew/add", "/whatsnew/edit/**", "/whatsnew/delete/**").hasRole("ADMIN")
                .requestMatchers("/messageboard/add", "/messageboard/edit/**", "/messageboard/delete/**", "/messageboard/save", "/messageboard/toggle/**", "/messageboard/bulk/**").hasRole("ADMIN")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Any other request requires authentication
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/perform_login")
                .defaultSuccessUrl("/", true)
                .successHandler(authenticationSuccessHandler(userService))
                .failureUrl("/login?error=true")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            .exceptionHandling(exceptions -> exceptions
                .accessDeniedPage("/access-denied")
            );
            
        return http.build();
    }
    
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler(@Lazy UserService userService) {
        return (request, response, authentication) -> {
            // Update last login time
            String username = authentication.getName();
            userService.updateLastLogin(username);
            
            // Redirect to home page
            response.sendRedirect("/");
        };
    }
} 