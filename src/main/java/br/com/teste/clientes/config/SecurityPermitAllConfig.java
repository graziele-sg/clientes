package br.com.teste.clientes.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
public class SecurityPermitAllConfig extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/api/**");
    }

    @Override
    protected void configure (HttpSecurity http) throws Exception {
        http.authorizeRequests().antMatchers("/").permitAll()
                .and().authorizeRequests().antMatchers("/h2-console/**").permitAll()
                .and().authorizeRequests().antMatchers("/actuator/**").permitAll();
        http.csrf().disable();
        http.headers().frameOptions().disable();
    }
}
