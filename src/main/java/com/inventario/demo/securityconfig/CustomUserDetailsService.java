package com.inventario.demo.securityconfig;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.inventario.demo.entity.Usuario;
import com.inventario.demo.repository.UsuarioRepository;

public class CustomUserDetailsService implements UserDetailsService{

	//Vamos a llamar al metodo de busqueda por nombre para autenticar al usuario
	@Autowired
	private UsuarioRepository usuarioRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<Usuario> usuario = usuarioRepository.findByUsername(username);	
		return usuario.map(CustomUserDetails::new).orElseThrow(() -> new UsernameNotFoundException("User not found whit this name :" + username));
	}
	
	

}
