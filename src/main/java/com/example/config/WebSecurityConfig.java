package com.example.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    private AuthTokenFilter authenticationJwtTokenFilter;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(unauthorizedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        // Public resources
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/lib/**", "/favicon.ico").permitAll()

                        // Public APIs - không cần đăng nhập
                        .requestMatchers("/api/auth/**").permitAll()

                        // Public pages
                        .requestMatchers("/", "/home", "/signup", "/login", "/about",
                                "/contact", "/privacy", "/terms", "/faq",
                                "/instructor", "/team", "/testimonial").permitAll()

                        // Admin only
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")

                        // Các API cần xác thực
                        .requestMatchers("/api/study-session/**").authenticated()
                        .requestMatchers("/api/progress/**").authenticated()
                        .requestMatchers("/study/**").authenticated()
                        .requestMatchers("/profile", "/settings").authenticated()

                        .anyRequest().permitAll()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);


        http.addFilterBefore(authenticationJwtTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}