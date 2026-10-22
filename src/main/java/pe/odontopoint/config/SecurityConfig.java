package pe.odontopoint.config;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import pe.odontopoint.repository.UserRepository;

@Configuration
public class SecurityConfig {
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  UserDetailsService userDetailsService(UserRepository users) {
    return username ->
        users
            .findByUsername(username.toLowerCase(java.util.Locale.ROOT))
            .map(
                u ->
                    User.withUsername(u.getUsername())
                        .password(u.getPasswordHash())
                        .roles(u.getRole())
                        .build())
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
  }

  @Bean
  SecurityFilterChain security(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
            a ->
                a.requestMatchers(
                        "/login", "/setup", "/css/**", "/js/auth.js", "/images/**", "/error")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .formLogin(
            f ->
                f.loginPage("/login")
                    .defaultSuccessUrl("/", true)
                    .failureUrl("/login?error")
                    .permitAll())
        .logout(l -> l.logoutSuccessUrl("/login?logout"))
        .headers(
            h ->
                h.contentSecurityPolicy(
                    c ->
                        c.policyDirectives(
                            "default-src 'self'; script-src 'self'; style-src 'self'; img-src"
                                + " 'self' data: blob:; object-src 'none'; frame-ancestors 'none';"
                                + " base-uri 'self'; form-action 'self'")))
        .exceptionHandling(
            e ->
                e.authenticationEntryPoint(
                    (request, response, error) -> {
                      if (!request.getRequestURI().startsWith("/api/")) {
                        new org.springframework.security.web.authentication
                                .LoginUrlAuthenticationEntryPoint("/login")
                            .commence(request, response, error);
                        return;
                      }
                      response.setStatus(401);
                      response.setContentType("application/json");
                      response
                          .getWriter()
                          .write("{\"message\":\"La sesión expiró. Vuelva a iniciar sesión.\"}");
                    }));
    return http.build();
  }
}
