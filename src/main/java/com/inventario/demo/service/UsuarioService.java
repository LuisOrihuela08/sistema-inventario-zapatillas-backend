package com.inventario.demo.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.inventario.demo.entity.Rol;
import com.inventario.demo.entity.Usuario;
import com.inventario.demo.repository.RolRepository;
import com.inventario.demo.repository.UsuarioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;


@Service
public class UsuarioService{

	@Autowired
	private UsuarioRepository usuarioRepository;
	
	@Autowired
	private RolRepository rolRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private JwtService jwtService;
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	public List<Usuario> listUsuarios(){
		//return usuarioRepository.findAll(Sort.by(Sort.Order.asc("usuario_id")));
		return usuarioRepository.findAll();
	}
	
	public Usuario save (Usuario usuario) {
		usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
		usuario.setRol(getRoles(usuario));
		return usuarioRepository.save(usuario);
	}
	//Esto es para asignarle más de un rol, si en caso habilitamos que sea ADMIN
	private List<Rol> getRoles(Usuario usuario){
		List<Rol> roles = new ArrayList<>();
		Optional<Rol> rolOptional = rolRepository.findByName("ROLE_USER");
		rolOptional.ifPresent(roles::add);
		if (usuario.isAdmin()) {
			Optional<Rol> adminRolOptional = rolRepository.findByName("ROLE_ADMIN");
			adminRolOptional.ifPresent(role -> roles.add(role));
		}
		return roles;
	}
	
	public Optional<Usuario> getOne (int id){
		return usuarioRepository.findById(id);
	}
	
	public void delete (int id) {
		usuarioRepository.deleteById(id);
	}
	
	public Optional<Usuario> obtenerUsuario (String username) {
		return usuarioRepository.findByUsername(username);
	}
	
	//para obtener los roles del usuario e inyectarlo en JwtService
	public List<Rol> getRolesByUsername(String username){
		return usuarioRepository.findByUsername(username).map(Usuario::getRol)
														 .orElse(Collections.emptyList());
	}
	
	
	public String generateToken(String username) {
		return jwtService.generateToken(username);
	}
	

	public void validateToken(String token) {
	    String username = jwtService.extractUsername(token);
	    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
	    if (!jwtService.validateToken(token, userDetails)) {
	        throw new RuntimeException("Token inválido");
	    }
}

}
