package com.cg.security;

import com.cg.configuration.CustomAccessDeniedHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public AccessDeniedHandler accessDeniedHandler(){
        return new CustomAccessDeniedHandler();
    }

    @Autowired
    public void configureGlobalSecurity(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser("user").password("{noop}user").roles("USER");
        auth.inMemoryAuthentication().withUser("admin").password("{noop}admin").roles("ADMIN", "USER");
        auth.inMemoryAuthentication().withUser("dba").password("{noop}dba").roles("ADMIN","DBA", "USER");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.authorizeRequests().antMatchers("/", "/login", "/logout").permitAll();
        http.authorizeRequests().antMatchers("/home").access("hasAnyRole('USER', 'ADMIN', 'DBA')");
        http.authorizeRequests().antMatchers("/admin").access("hasAnyRole('ADMIN', 'DBA')");
        http.authorizeRequests().antMatchers("/dba").access("hasRole('DBA')");

        http.authorizeRequests()
            .and().formLogin()
            .loginProcessingUrl("/login")
            .loginPage("/login")
            .defaultSuccessUrl("/home")
            .failureUrl("/login?error=true")
            .usernameParameter("username")
            .passwordParameter("password")
            .and().logout().logoutUrl("/logout").logoutSuccessUrl("/login")
            .and().exceptionHandling().accessDeniedHandler(accessDeniedHandler());

    }
}