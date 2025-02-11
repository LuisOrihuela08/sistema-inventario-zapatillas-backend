package com.inventario.demo.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inventario.demo.dto.UsuarioDTO;
import com.inventario.demo.entity.Usuario;
import com.inventario.demo.service.UsuarioService;

@RestController
@RequestMapping("/api-usuario")
@CrossOrigin(origins = "http://localhost:4200", methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
		RequestMethod.DELETE, RequestMethod.OPTIONS })
public class UsuarioController {

	// Esto es para mandar mensajes
	private final Logger logger = LoggerFactory.getLogger(UsuarioController.class);

	@Autowired
	private UsuarioService usuarioService;

	@Autowired
	private AuthenticationManager authenticationManager;

	@GetMapping("/list/usuario")
	public ResponseEntity<?> findAllUsuarios() {
		try {
			List<Usuario> listAllUsuarios = usuarioService.listUsuarios();
			logger.info("UsuarioController::findAllUsuarios listando a los usuario");

			return new ResponseEntity<List<Usuario>>(listAllUsuarios, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error inesperado en findAllUsuarios: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno del servidor");
		}

	}

	// Métod para listar a los usuarios con paginacion y es la que se esta
	// consumiendo en el frontend -- ROL_ADMIN
	@GetMapping("/list-usuarios-paginados")
	public ResponseEntity<?> getAllUsuariosPaginados(@RequestParam int page, @RequestParam int size) {
		try {
			Page<Usuario> listUsuariosPaginados = usuarioService.findAllUsuariosPaginados(page, size);
			logger.info("Los usuarios encontrados son: ", listUsuariosPaginados);
			return new ResponseEntity<Page<Usuario>>(listUsuariosPaginados, HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Hubo un error al obtener los usuarios por paginacion", e.getMessage());
			return ResponseEntity.internalServerError().body("Error al obtener los usuarios paginados");
		}

	}

	@PostMapping("/add/usuario")
	public ResponseEntity<Usuario> guardarUsuario(@RequestBody Usuario usuario) {
		logger.info("UserController::createUser: creando {}", usuario);
		return new ResponseEntity<>(usuarioService.save(usuario), HttpStatus.CREATED);
	}

	@PutMapping("/update/usuario/{id}")
	public ResponseEntity<?> updateUser(@PathVariable("id") int id, @RequestBody UsuarioDTO usuarioDTO) {
		Usuario usuario = usuarioService.getOne(id).get();
		usuario.setUsername(usuarioDTO.getUsername());
		usuario.setPassword(usuarioDTO.getPassword());
		usuario.setNombre(usuarioDTO.getNombre());
		usuario.setApellido(usuarioDTO.getApellido());
		usuario.setCorreo(usuarioDTO.getCorreo());
		usuario.setFecha_nac(usuarioDTO.getFecha_nac());
		usuarioService.save(usuario);
		return new ResponseEntity<>("Usuario Editado exitosamente", HttpStatus.OK);
	}

	@DeleteMapping("/delete/usuario/{id}")
	public ResponseEntity<?> deleteUsuario(@PathVariable("id") int id) {
		usuarioService.delete(id);
		return new ResponseEntity<>("Usuario Elimnado", HttpStatus.OK);
	}

	@GetMapping("/nombre-usuario")
	public ResponseEntity<?> buscarNombreDeUsuariAutenticado(Authentication authentication) {

		try {
			String username = authentication.getName();

			Optional<Usuario> optionalUsuario = usuarioService.obtenerUsuario(username);

			if (optionalUsuario.isEmpty()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Collections.singletonMap("message", "Usuario no encontrado"));
			}
			Usuario usuario = optionalUsuario.get(); // Obtenemos al usuario si esta presente y validando no solo la
			// authenticacion sino tambien la verificacion de que el usuario
			// esta registrado en la bd

			String nombreUsuarioAutenticado = usuario.getNombre();

			if (nombreUsuarioAutenticado.isEmpty()) {
				return new ResponseEntity<>("Nombre del usuario no encontrado: " + null, HttpStatus.NOT_FOUND);
			}
			logger.info("Nombre del usuario: " + nombreUsuarioAutenticado);
			return ResponseEntity.ok(Collections.singletonMap("nombre_usuario", nombreUsuarioAutenticado));
			// return new ResponseEntity<>(nombreUsuarioAutenticado, HttpStatus.OK); // esto
			// nos devuelve el nombre el json pero solo el nombre plano

		} catch (UsernameNotFoundException e) {
			logger.error("Error: Usuario no encontrado", e);
			return new ResponseEntity<>("Usuario no encontrado", HttpStatus.UNAUTHORIZED);// 401
		} catch (AccessDeniedException e) {
			logger.error("Error: Acceso denegado", e);
			return new ResponseEntity<>("No tienes permisos para hacer esta petición", HttpStatus.FORBIDDEN); // 403
		} catch (Exception e) {
			logger.error("Error desconocido al encontrar el nombre", e);
			return new ResponseEntity<>("Error en el servidor ", HttpStatus.INTERNAL_SERVER_ERROR);// 500
		}
	}
	
	//Este método es para obtener los datos del usuario autenticado
	@GetMapping("/usuario-perfil")
	public ResponseEntity<?> getPerfilUsuario (Authentication authentication){
		
		try {
			String username = authentication.getName();
			
			Optional<Usuario> usuarioOptional = usuarioService.obtenerUsuario(username);
			
			if (usuarioOptional.isEmpty()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Collections.singletonMap("message", "Usuario no encontrado"));
			}
			
			Usuario usuario = usuarioOptional.get();
			UsuarioDTO usuarioDTO = new UsuarioDTO(usuario);
			Map<String, Object> usuarioMap = new HashMap<>();
			usuarioMap.put("message", "Perfil del usuario obtenido con éxito");
			usuarioMap.put("usuario", usuarioDTO);

			logger.info("Perfil del usuario "+ usuarioDTO);
			return new ResponseEntity<>(usuarioMap, HttpStatus.OK);
			
 		} catch (UsernameNotFoundException e) {
			logger.error("Error: Usuario no encontrado", e);
			return new ResponseEntity<>("Usuario no encontrado", HttpStatus.UNAUTHORIZED);// 401
		} catch (AccessDeniedException e) {
			logger.error("Error: Acceso denegado", e);
			return new ResponseEntity<>("No tienes permisos para hacer esta petición", HttpStatus.FORBIDDEN); // 403
		} catch (Exception e) {
			logger.error("Error desconocido al encontrar el nombre", e);
			return new ResponseEntity<>("Error en el servidor ", HttpStatus.INTERNAL_SERVER_ERROR);// 500
		}
	}

	/*
	 * //Aca me genera el token y me lo muestra en formato json
	 * 
	 * @PostMapping("/token") public ResponseEntity<?> getToken(@RequestBody
	 * UsuarioDTO usuarioDTO) { Authentication authenticate =
	 * authenticationManager.authenticate(new
	 * UsernamePasswordAuthenticationToken(usuarioDTO.getUsername(),
	 * usuarioDTO.getPassword()));
	 * 
	 * if (authenticate.isAuthenticated()) { String token =
	 * usuarioService.generateToken(usuarioDTO.getUsername()); Map<String, String>
	 * response = new HashMap<>(); response.put("token", token); return new
	 * ResponseEntity<>(response, HttpStatus.OK); } else { throw new
	 * RuntimeException("invalid access"); }
	 * 
	 * }
	 */
	@PostMapping("/token")
	public ResponseEntity<String> getToken(@RequestBody UsuarioDTO usuarioDTO) {
		Authentication authenticate = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(usuarioDTO.getUsername(), usuarioDTO.getPassword()));

		if (authenticate.isAuthenticated()) {
			String token = usuarioService.generateToken(usuarioDTO.getUsername());
			return new ResponseEntity<>(token, HttpStatus.OK); // Devuelve solo el token como String
		} else {
			throw new RuntimeException("invalid access");
		}
	}

	@GetMapping("/validate")
	public String validateToken(@RequestParam("token") String token) {
		usuarioService.validateToken(token);
		return "token es válido";
	}

}
