package com.inventario.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.inventario.demo.entity.Rol;
import com.inventario.demo.entity.Usuario;
import com.inventario.demo.repository.RolRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import io.jsonwebtoken.Claims;


@Component
public class JwtService {
	
	@Autowired
	@Lazy
	private UsuarioService usuarioService;

	public static final String SECRET = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";


	public boolean validateToken(String token, UserDetails userDetails) {
	    final String username = extractUsername(token);
	    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}
    
    
       public String generateToken(String userName) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userName);
    }
      
    private String createToken(Map<String, Object> claims, String userName) {
    	  Optional<Usuario> usuario = usuarioService.obtenerUsuario(userName);
    	    
    	    if (usuario.isPresent()) {
    	        List<Rol> listRoles = usuarioService.getRolesByUsername(userName);
    	        claims.put("roles", listRoles);
    	        claims.put("apellido", usuario.get().getApellido()); // Asegúrate de acceder al objeto usuario con .get()
    	        claims.put("correo", usuario.get().getCorreo());   // Igual aquí
    	        
    	        return Jwts.builder()
    	                .setClaims(claims)
    	                .setSubject(userName)
    	                .setIssuedAt(new Date(System.currentTimeMillis()))
    	                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30))
    	                .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
    	    } else {
    	        // Lógica en caso de que no se encuentre el usuario
    	        throw new UsernameNotFoundException("Usuario no encontrado: " + userName);
    	    }
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    //Este metodo es para extraer el usuario del token
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    
    private boolean isTokenExpired(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .before(new Date());
    }
    
    public List<String> extractRoles(String token) {
        Claims claims = extractClaims(token);
        Object rolesObj = claims.get("roles");

        if (rolesObj instanceof List<?>) {
            List<?> rolesList = (List<?>) rolesObj;
            return rolesList.stream()
                .filter(role -> role instanceof LinkedHashMap) // Validar que es un mapa
                .map(role -> ((LinkedHashMap<?, ?>) role).get("name").toString()) // Extraer "name"
                .collect(Collectors.toList());
        }
        return List.of(); // Si no se encuentran roles, devolver lista vacía
    }


 // Método para extraer los claims del token
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET)  // La clave secreta debe coincidir con la usada para firmar el JWT
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


}
