package ru.kubarev.releaseflow.config;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.SecurityFilterChain;
import ru.kubarev.releaseflow.repository.UserRepository;
@Configuration public class SecurityConfig {
 @Bean PasswordEncoder encoder(){return new BCryptPasswordEncoder();}
 @Bean UserDetailsService details(UserRepository users){return name->{var u=users.findByUsername(name.trim().toLowerCase(java.util.Locale.ROOT)).orElseThrow(()->new UsernameNotFoundException("User not found"));return User.withUsername(u.getUsername()).password(u.getPassword()).authorities(u.getRoles().stream().map(r->"ROLE_"+r.name()).toArray(String[]::new)).build();};}
 @Bean SecurityFilterChain chain(HttpSecurity http,UserRepository users)throws Exception{
  return http.authorizeHttpRequests(a->a.requestMatchers("/","/about","/login","/register","/assets/**","/error").permitAll()
   .requestMatchers("/admin/**","/actuator/**").access((auth,context)->new org.springframework.security.authorization.AuthorizationDecision(users.findByUsername(auth.get().getName()).map(u->u.isAdmin()).orElse(false))).anyRequest().authenticated())
   .formLogin(f->f.loginPage("/login").defaultSuccessUrl("/dashboard",true).permitAll())
   .logout(l->l.logoutSuccessUrl("/login?logout").permitAll())
   .exceptionHandling(e->e.accessDeniedPage("/forbidden")).build();
 }
}
