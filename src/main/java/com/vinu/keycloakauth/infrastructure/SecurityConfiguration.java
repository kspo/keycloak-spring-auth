package com.vinu.keycloakauth.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	private static final String[] PUBLIC_PATHS = {
			"/common/**",
			"/healthcheck",
			"/documentation/**"
	};

	private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
	private final CustomAccessDeniedHandler customAccessDeniedHandler;

	public SecurityConfiguration(CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
								 CustomAccessDeniedHandler customAccessDeniedHandler) {
		this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
		this.customAccessDeniedHandler = customAccessDeniedHandler;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.exceptionHandling(exceptionHandling -> exceptionHandling
						.authenticationEntryPoint(customAuthenticationEntryPoint)
						.accessDeniedHandler(customAccessDeniedHandler)
				)
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(PUBLIC_PATHS).permitAll()
						.requestMatchers("/api/v1/user/**").hasAuthority("REALM_USER")
						.requestMatchers("/api/v1/admin/**").hasAuthority("REALM_ADMIN")
						.anyRequest().authenticated()
				).sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				)
				.oauth2ResourceServer(oauth2 -> oauth2
						.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
				);
		return http.build();
	}

	private JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
		return converter;
	}

}
