package com.inventario.demo.securityconfig;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.inventario.demo.controller.UsuarioController;
import com.inventario.demo.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter{

	private final Logger logger = LoggerFactory.getLogger(UsuarioController.class);
	
	@Autowired
	private JwtService jwtService;
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		String authHeader = request.getHeader("Authorization");
		
		//esto es para ver como viene el token
		logger.info("Authorization Header: {}", authHeader);
		
        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // Extraer el token sin "Bearer "
            username = jwtService.extractUsername(token); // Método para extraer el usuario del token
        }
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
           
            
            if (jwtService.validateToken(token, userDetails)) { // Validar el token con los datos del usuario
            	/////////////////
            	List<String> roles = jwtService.extractRoles(token);
            	List<GrantedAuthority> authorities = roles.stream()
            											  .map(SimpleGrantedAuthority::new)
            											  .collect(Collectors.toList());
            	 logger.info("Usuario autenticado: {}", username);
                 logger.info("Roles extraídos del token: {}", roles);
                 logger.info("Authorities asignadas: {}", authorities);
            	
            	//////////////////////
            	UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }else {
            	logger.warn("Token inválido para el usuario: {}", username);
            }
            
        } else {
        	 logger.warn("No se pudo extraer el username del token.");
        }

     
        filterChain.doFilter(request, response);
		
	}

}
