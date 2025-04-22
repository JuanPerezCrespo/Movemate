package es.upm.grupo19.isst.movematefront.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests((requests) -> requests
						.requestMatchers("/login", "/registroClientes", "/registroMonitores", "/css/**", "/Images/**")
						.permitAll()
						.anyRequest().authenticated())
				.formLogin((form) -> form
						.loginPage("/login")
						.defaultSuccessUrl("/inicio", true)
						.failureUrl("/login?error=true") // Redirigir con un parámetro de error si las credenciales son
															// incorrectas
						.permitAll())
				.logout((logout) -> logout.permitAll());

		return http.build();
	}
}
