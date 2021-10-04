package com.wrox.config;


import com.wrox.site.services.UserPrincipalService;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.inject.Inject;

@Configuration
@EnableWebMvcSecurity
@EnableGlobalMethodSecurity(
        prePostEnabled = true, order = 0, mode = AdviceMode.PROXY,
        proxyTargetClass = false
)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter
{
    @Inject
    UserPrincipalService userService;

    @Bean
    protected SessionRegistry sessionRegistryImpl()
    {
        return new SessionRegistryImpl();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception
    {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder builder)
            throws Exception
    {
        builder
                .userDetailsService(this.userService)
                .passwordEncoder(new BCryptPasswordEncoder())
                .and()
                .eraseCredentials(true);
    }

    @Override
    public void configure(WebSecurity security)
    {
        security.ignoring().antMatchers("/api/*");
    }

    @Override
    protected void configure(HttpSecurity security) throws Exception
    {
        security.csrf().disable()
                .authorizeRequests()
                .and().formLogin()
                .loginProcessingUrl("/perform_login")
                .loginPage("/api/login").failureUrl("/api/login?loginFailed")
                .defaultSuccessUrl("/api/profiles/currentUser",true)
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
                .and().logout()
                .logoutUrl("/logout").logoutSuccessUrl("/api/login?loggedOut")
                .invalidateHttpSession(true).deleteCookies("JSESSIONID")
                .permitAll()
                .and().sessionManagement()
                .maximumSessions(1).maxSessionsPreventsLogin(true)
                .sessionRegistry(this.sessionRegistryImpl());
    }
}
