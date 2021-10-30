package com.wrox.config;


import com.wrox.site.CorsFilter;
import com.wrox.site.JwtAuthenticationFilter;
import com.wrox.site.services.UserPrincipalService;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.inject.Inject;
import java.util.logging.Filter;

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
    JwtAuthenticationFilter jwtAuthenticationFilter(){return new JwtAuthenticationFilter();}

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
                .passwordEncoder(new BCryptPasswordEncoder());
//                .and()
//                .eraseCredentials(true);
    }

//    @Override
//    public void configure(WebSecurity security)
//    {
//        security.ignoring().antMatchers("/api/*");
//    }

    @Override
    protected void configure(HttpSecurity security) throws Exception
    {
        security.csrf().disable()
                .authorizeRequests().antMatchers("/admin/*").hasAuthority("Administrator")
                .and().formLogin()
                .loginPage("/admin/login").failureUrl("/admin/login?loginFailed")
                .defaultSuccessUrl("/admin/events")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
                .and().logout()
                .logoutUrl("/admin/logout").logoutSuccessUrl("/admin/login?loggedOut")
                .invalidateHttpSession(true).deleteCookies("JSESSIONID")
                .permitAll();
        security.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }
}
