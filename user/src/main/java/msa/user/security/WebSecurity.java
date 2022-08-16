package msa.user.security;

import lombok.RequiredArgsConstructor;
import msa.user.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.servlet.Filter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurity {
    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final Environment env;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        AuthenticationManager authenticationManager = getAuthenticationFilter(http);
        http.csrf().disable();
//        http.authorizeRequests().antMatchers("/users/**").permitAll();
        http.authorizeRequests()
                .antMatchers("/actuator/**").permitAll()
                .antMatchers("/error/**").permitAll()
                .antMatchers("/**").hasIpAddress("172.30.1.39")
                .and()
                .authenticationManager(authenticationManager)
                .addFilter(getAuthenticationFilter(authenticationManager));
        http.headers().frameOptions().disable();    // frame load 하게 해줌

        return http.build();
    }

    private AuthenticationManager getAuthenticationFilter(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder);
        return authenticationManagerBuilder.build();
    }

    private AuthenticationFilter getAuthenticationFilter(AuthenticationManager authenticationManager) {
        return new AuthenticationFilter(authenticationManager, userService, env);
    }
}

//@Configuration
//@EnableWebSecurity
//@RequiredArgsConstructor
//public class WebSecurity extends WebSecurityConfigurerAdapter{
//    private final UserService userService;
//    private final BCryptPasswordEncoder bCryptPasswordEncoder;
//    private final Environment env;
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http.csrf().disable();
//        // http.authorizeRequests().antMatchers("/users/**").permitAll();  // 모든 요청에 대해서 permission 허용
//        http.authorizeRequests().antMatchers("/**")
//            .hasIpAddress("172.30.1.11")
//            .and()
//            .addFilter(getAuthenticationFilter());
//        http.headers().frameOptions().disable();
//    }
//
//    private AuthenticationFilter getAuthenticationFilter() throws Exception {
//        AuthenticationFilter authenticationFilter =
//                new AuthenticationFilter(authenticationManager(), userService, env);
//        return authenticationFilter;
//    }
//
//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder);
//    }
//}