package com.pedalgenie.vote.global.config;

import com.pedalgenie.vote.domain.jwt.filter.JwtAuthenticationFilter;
import com.pedalgenie.vote.domain.jwt.JwtUtil;
import com.pedalgenie.vote.domain.jwt.filter.JwtValidationFilter;
import com.pedalgenie.vote.domain.jwt.TokenValidator;
import com.pedalgenie.vote.domain.member.repostiory.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtUtil jwtUtil;
    private final TokenValidator tokenValidator;
    private final MemberRepository memberRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{

        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(HttpBasicConfigurer::disable);

        http
                // 접근 허용된 URI
                .authorizeHttpRequests(auth ->auth
                        .requestMatchers("/login", "/", "/api/auth").permitAll()
                        .requestMatchers("/swwagger-ui/index.html", "/v3/api-docs/**", "http://localhost:3000/**").permitAll()
                        .anyRequest().authenticated());

        // AuthenticationManager 생성
        AuthenticationManager authenticationManager = authenticationManager(authenticationConfiguration);

        // JwtAuthenticationFilter 설정
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager, jwtUtil,memberRepository);
        jwtAuthenticationFilter.setFilterProcessesUrl("/login"); // 로그인 URL 설정

        // 필터 추가
        http
                .addFilterBefore(new JwtValidationFilter(jwtUtil, tokenValidator,memberRepository), JwtAuthenticationFilter.class)
                .addFilterAt(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        http
                .sessionManagement((session)->session.sessionCreationPolicy(STATELESS));

        return http.build();
    }

    // AuthenticationManager 빈 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
