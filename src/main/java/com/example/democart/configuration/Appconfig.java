package com.example.democart.configuration;
import java.beans.BeanProperty;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration 
public class Appconfig {
    @Bean
    public SecurityFilterChain securityfilterchain(HttpSecurity http) throws Exception{
       http
        .authorizeHttpRequests((authorize) -> authorize
	    .requestMatchers("/").hasAuthority("USER")
            .anyRequest().authenticated()
        );
       return null;
        
    }
    
    

    
}
