package com.jelly.zzirit.global.config.securityconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jelly.zzirit.domain.member.entity.authenum.Role;
import com.jelly.zzirit.global.config.securityconfig.oauth2config.ClientRegistrationRepository;
import com.jelly.zzirit.global.exception.CustomAccessDeniedHandler;
import com.jelly.zzirit.global.exception.CustomAuthenticationEntryPoint;
import com.jelly.zzirit.global.security.filter.JwtFilter;
import com.jelly.zzirit.global.security.filter.LoginFilter;
import com.jelly.zzirit.global.security.handler.CustomLogoutHandler;
import com.jelly.zzirit.global.security.handler.CustomLogoutSuccessHandler;
import com.jelly.zzirit.global.security.oauth2.handler.CustomOAuth2FailureHandler;
import com.jelly.zzirit.global.security.oauth2.handler.CustomOAuth2SuccessHandler;
import com.jelly.zzirit.global.security.oauth2.service.CustomOAuth2UserService;
import com.jelly.zzirit.global.security.service.TokenService;
import com.jelly.zzirit.global.security.util.AccountLoginRateUtil;
import com.jelly.zzirit.global.security.util.JwtUtil;
import com.jelly.zzirit.global.security.util.RedisBlacklistUtil;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final CorsConfig corsConfig;
	private final CustomOAuth2UserService customOAuth2UserService;
	private final CustomLogoutHandler customLogoutHandler;
	private final CustomLogoutSuccessHandler customLogoutSuccessHandler;
	private final ClientRegistrationRepository clientRegistrationRepository;
	private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
	private final CustomOAuth2FailureHandler customOAuth2FailureHandler;
	private final CustomAuthenticationEntryPoint authenticationEntryPoint;
	private final CustomAccessDeniedHandler accessDeniedHandler;
	private final RedisBlacklistUtil redisBlacklistUtil;
	private final TokenService tokenService;
	private final JwtUtil jwtUtil;

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	@Bean
	public LoginFilter loginFilter(
		AuthenticationManager authenticationManager,
		ObjectMapper objectMapper,
		TokenService tokenService,
		AccountLoginRateUtil accountLoginRateUtil) {
		return new LoginFilter(authenticationManager, objectMapper, tokenService, accountLoginRateUtil);
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, LoginFilter loginFilter) throws Exception {
		JwtFilter jwtFilter = new JwtFilter(redisBlacklistUtil, tokenService, jwtUtil);

		http
			.cors(cors -> cors.configurationSource(corsConfig));

		http
			.csrf(AbstractHttpConfigurer::disable);

		http
			.formLogin(AbstractHttpConfigurer::disable);

		http
			.httpBasic(AbstractHttpConfigurer::disable);

		http
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()
				.requestMatchers("/api/info/temp-check").permitAll()
				.requestMatchers("/oauth2/authorization/**", "/login/oauth2/code/**").permitAll()
				.requestMatchers(
					"/docs/**",
					"/actuator/**",
					"/favicon.ico"
				).permitAll()
				.requestMatchers("/api/admin/**").hasRole(Role.ROLE_ADMIN.getKey())
				.anyRequest().authenticated()
			);

		http
			.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

		http
			.addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);

		http
			.exceptionHandling(exception -> exception
				.authenticationEntryPoint(authenticationEntryPoint)  // 인증 실패 시 실행
				.accessDeniedHandler(accessDeniedHandler)  // 권한 부족 시 실행
			);

		http
			.oauth2Login((oauth2) -> oauth2
				.userInfoEndpoint(
					(userInfoEndpointConfig -> userInfoEndpointConfig.userService(customOAuth2UserService)))
				.successHandler(customOAuth2SuccessHandler)
				.failureHandler(customOAuth2FailureHandler)
				.clientRegistrationRepository(clientRegistrationRepository.clientRegistrationRepository())
			);

		http
			.logout(logout -> logout
				.logoutUrl("/api/auth/logout")
				.addLogoutHandler(customLogoutHandler)
				.logoutSuccessHandler(customLogoutSuccessHandler)
				.permitAll());

		http
			.sessionManagement(session -> session
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		return http.build();
	}
}