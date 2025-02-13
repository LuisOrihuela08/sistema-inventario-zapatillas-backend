package com.inventario.demo.securityconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.inventario.demo.controller.UsuarioController;


@Configuration
@EnableWebSecurity
public class AuthConfig {
	
	private final Logger logger = LoggerFactory.getLogger(UsuarioController.class);

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	/*Para validar el token*/
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
	
	/*Esto es para autenticar el token*/
	@Bean
	public UserDetailsService userDetailsService() {
		return new CustomUserDetailsService();
	}
	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
		authenticationProvider.setUserDetailsService(userDetailsService());
		authenticationProvider.setPasswordEncoder(passwordEncoder());
		return authenticationProvider;
	}
	
	@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        logger.info("Cargando configuración de seguridad...");

        SecurityFilterChain securityFilterChain = http
        	.cors().and().csrf().disable() //Esto es importante porque omite una primera peticion hacia el backen y como este tiene seguridad, no envia el token al primer intento dificultando la autentacion y obtencion de datos de el usuario
            .authorizeHttpRequests(auth -> {
            	auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll(); //Esto es porque des
                auth.requestMatchers("/api-usuario/add/usuario",
                                     "/api-usuario/token",
                                     "/api-usuario/validate").permitAll();              
                auth.requestMatchers("/api-usuario/list-usuarios-paginados",
                		             "/api-usuario/delete/usuario/{id}",
                					 "/api-inventario/list-all/inventario",
                                     "/api-inventario/list/inventario",
                                     "/api-inventario/list-all/zapatillas",
                                     "/api-inventario/list/zapatillas").hasAnyAuthority("ROLE_ADMIN");     
                auth.requestMatchers("/api-usuario/nombre-usuario",
                					 "/api-usuario/usuario-perfil",
                					 "/api-usuario/update-usuario-perfil").hasAnyAuthority("ROLE_USER");
                auth.requestMatchers("/api-inventario/**").authenticated();
                

                logger.info("Reglas de autorización configuradas correctamente.");
            })
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();

        logger.info("Configuración de seguridad aplicada exitosamente.");
        
        return securityFilterChain;
    }
	
	
}
