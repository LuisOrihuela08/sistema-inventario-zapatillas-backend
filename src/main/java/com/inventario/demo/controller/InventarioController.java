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
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.inventario.demo.dto.InventarioDTO;
import com.inventario.demo.dto.ZapatillaDTO;
import com.inventario.demo.entity.Estado;
import com.inventario.demo.entity.Inventario;
import com.inventario.demo.entity.Usuario;
import com.inventario.demo.entity.Zapatilla;
import com.inventario.demo.service.EstadoService;
import com.inventario.demo.service.InventarioService;
import com.inventario.demo.service.UsuarioService;
import com.inventario.demo.service.ZapatillaService;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMethod;

@RestController
@CrossOrigin(origins = "http://localhost:4200/", methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,RequestMethod.DELETE, RequestMethod.OPTIONS })//Esta URL es para pruebas de manera local
//@CrossOrigin(origins = "http://localhost", methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,RequestMethod.DELETE, RequestMethod.OPTIONS })//Esto es para aceptar peticiones desde el frontend dockerizado el puerto 80
@RequestMapping("/api-inventario")
public class InventarioController {

	private final Logger logger = LoggerFactory.getLogger(InventarioController.class);

	@Autowired
	private ZapatillaService zapatillaService;

	@Autowired
	private InventarioService inventarioService;

	@Autowired
	private UsuarioService usuarioService;
	
	@Autowired
	private EstadoService estadoService;

	// Inventario CRUD
	// Listar sin paginacion -- ROLE_ADMIN
	@GetMapping("/list-all/inventario")
	public ResponseEntity<List<Inventario>> findAllInventarios() {
		List<Inventario> listAllInventarios = inventarioService.listInventario();
		return new ResponseEntity<List<Inventario>>(listAllInventarios, HttpStatus.OK);
	}

	// Listar con paginacion -- ROLE_ADMIN
	@GetMapping("/list/inventario")
	public ResponseEntity<Page<Inventario>> findPageInventario(@RequestParam int page, @RequestParam int size) {
		Page<Inventario> listPageInventario = inventarioService.listPageInventario(page, size);
		return new ResponseEntity<>(listPageInventario, HttpStatus.OK);
	}
	
	//Este método es el que se esta usando en el frontend ya que lista con paginacion
	//Además tambien muestra los inventario por usuario
	@GetMapping("/inventario-usuario-page")
	public ResponseEntity<?> getInventarioPageByUsuario(@RequestParam int page,
														@RequestParam int size,
														Authentication authentication){
		try {
			
			String username = authentication.getName();
			Optional<Usuario> optionalUsuario = usuarioService.obtenerUsuario(username);
			if (optionalUsuario.isEmpty()) {
				logger.error("Usuario no encontrado");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("message", "Usuario no encontrado"));
			}
			Usuario usuario = optionalUsuario.get();
			
			Page<Inventario> inventarioListPage = inventarioService.listInventarioPageByUsuario(page, size, usuario.getId());
			
			if (inventarioListPage.isEmpty()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Collections.singletonMap("message", "No se encontro inventario de este usuario"));
			}
						
			logger.info("Inventario paginado encontrado del usuario: ", usuario);
			return ResponseEntity.ok(inventarioListPage);
			 
		} catch (UsernameNotFoundException e) {
			logger.error("Error: Usuario no encontrado", e);
			return new ResponseEntity<>("Usuario no encontrado", HttpStatus.UNAUTHORIZED); // 401

		} catch (AccessDeniedException e) {
			logger.error("Error: Acceso denegado", e);
			return new ResponseEntity<>("No tienes permisos para realizar esta acción", HttpStatus.FORBIDDEN); // 403

		} catch (Exception e) {
			logger.error("Error desconocido al crear inventario", e);
			return new ResponseEntity<>("Error en el servidor", HttpStatus.INTERNAL_SERVER_ERROR); // 500
		}
	}

	// Listar por inventario por usuario -- ROLE_ADMIN y ROLE_USER
	@GetMapping("/inventario-usuario")
	public ResponseEntity<?> getInventarioByUsuario(Authentication authentication) {
		String username = authentication.getName();

		Optional<Usuario> optionalUsuario = usuarioService.obtenerUsuario(username);

		if (optionalUsuario.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Collections.singletonMap("message", "Usuario no encontrado"));
		}
		Usuario usuario = optionalUsuario.get(); // Obtenemos al usuario si esta presente

		List<Inventario> listInventarioByUsuario = inventarioService.findInventarioByUsuarioId(usuario.getId());

		if (listInventarioByUsuario.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Collections.singletonMap("message", "No se encontro inventario de este usuario"));
		}
		return ResponseEntity.ok(listInventarioByUsuario);
	}

	// -- ROLE_ADMIN y ROLE_USER
	@PostMapping("/add/inventario")
	public ResponseEntity<?> addInventario(@ModelAttribute InventarioDTO inventarioDTO,
										   @RequestParam("imagen") MultipartFile imagen, 
										   Authentication authentication) {
		
		try {
			// Obtener usuario autenticado
			String username = authentication.getName();
			Usuario usuario = usuarioService.obtenerUsuario(username)
					.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

			// Subimos la imagen de la zapatilla al metodo que creamos
			String imagenURL = zapatillaService.guardarImagen(imagen);

			// Y creamos la zapatilla con sus demás atributos
			Zapatilla zapatilla = new Zapatilla();
			zapatilla.setMarca(inventarioDTO.getMarca());
			zapatilla.setSilueta(inventarioDTO.getSilueta());
			zapatilla.setTalla(inventarioDTO.getTalla());
			zapatilla.setColorway(inventarioDTO.getColorway());
			zapatilla.setMateriales(inventarioDTO.getMateriales());
			zapatilla.setImagen(imagenURL);

			//Ahora obtenemos el id de un estado que se le va asignar a un Inventario
			Estado estado = estadoService.findById(inventarioDTO.getEstado_id()).orElseThrow(() -> new IllegalArgumentException("Estado no encontrado"));
			
					
			// Crear inventario
			Inventario inventario = new Inventario();
			inventario.setCantidad(inventarioDTO.getCantidad());
			inventario.setPrecio(inventarioDTO.getPrecio());
			inventario.setComentario(inventarioDTO.getComentario());
			inventario.setFecha_compra(inventarioDTO.getFecha_compra());
			inventario.setZapatilla(zapatilla);
			inventario.setUsuario(usuario);
			inventario.setEstado(estado);

			inventarioService.save(inventario);
			logger.info("Inventario creado exitosamente");
			logger.info("Inventario nuevo: {}", inventario);
			return new ResponseEntity<>(inventario, HttpStatus.CREATED);

		} catch (UsernameNotFoundException e) {
			logger.error("Error: Usuario no encontrado", e);
			return new ResponseEntity<>("Usuario no encontrado", HttpStatus.UNAUTHORIZED); // 401

		} catch (AccessDeniedException e) {
			logger.error("Error: Acceso denegado", e);
			return new ResponseEntity<>("No tienes permisos para realizar esta acción", HttpStatus.FORBIDDEN); // 403

		} catch (Exception e) {
			logger.error("Error desconocido al crear inventario", e);
			return new ResponseEntity<>("Error en el servidor", HttpStatus.INTERNAL_SERVER_ERROR); // 500
		}
	}

	@PutMapping("/update/inventario/{id}")
	public ResponseEntity<?> updateInventario(@PathVariable("id") int id, @ModelAttribute InventarioDTO inventarioDTO,
			@RequestParam(value = "imagen", required = false) MultipartFile imagen, // Esto es para manejar mas que nada
																					// una peticion desde el frontend si
			// en caso no se proporciona una // imagen nueva
			Authentication authentication) {

		try {
			// Obtener usuario autenticado
			String username = authentication.getName();
			Usuario usuario = usuarioService.obtenerUsuario(username)
					.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

			// Verficamos que el inventario existe
			Optional<Inventario> inventarioOptional = inventarioService.getOne(id);
			if (!inventarioOptional.isPresent()) {
				return new ResponseEntity<>("Inventario no encontrado", HttpStatus.NOT_FOUND);
			}
			Inventario inventario = inventarioService.getOne(id).get();

			// Ahora obtenemos la zapatilla asociada
			Zapatilla zapatilla = inventario.getZapatilla();

			if (zapatilla == null) {
				return new ResponseEntity<>("Zapatilla no encontrada", HttpStatus.NOT_FOUND);
			}

			// Actualizamos la imagen si se proporciona otra
			if (imagen != null && !imagen.isEmpty()) {
				String imagenAnterior = zapatilla.getImagen();

				String nuevaImagenURL = zapatillaService.guardarImagen(imagen);
				zapatilla.setImagen(nuevaImagenURL);

				if (imagenAnterior != null) {
					zapatillaService.eliminarImagen(imagenAnterior);
				}
			} else {
				// Si en caso no se proporciona ninguna imagen, se mantendra la que se encuentra
				// ya registrada en la bd
				zapatilla.setImagen(zapatilla.getImagen());
			}

			// Y actualizamos los atributos de las zapatillas
			zapatilla.setMarca(inventarioDTO.getMarca());
			zapatilla.setSilueta(inventarioDTO.getSilueta());
			zapatilla.setTalla(inventarioDTO.getTalla());
			zapatilla.setColorway(inventarioDTO.getColorway());
			zapatilla.setMateriales(inventarioDTO.getMateriales());

			//Ahora obtenemos el id de un estado que se le va asignar a un Inventario
			Estado estado = estadoService.findById(inventarioDTO.getEstado_id()).orElseThrow(() -> new IllegalArgumentException("Estado no encontrado"));
			
			
			// Y por ultimo actualizamos los atributos del inventario
			inventario.setCantidad(inventarioDTO.getCantidad());
			inventario.setPrecio(inventarioDTO.getPrecio());
			inventario.setComentario(inventarioDTO.getComentario());
			inventario.setFecha_compra(inventarioDTO.getFecha_compra());
			inventario.setZapatilla(zapatilla);
			inventario.setUsuario(usuario);
			inventario.setEstado(estado);

			inventarioService.save(inventario);
			logger.info("Inventario actualizado exitosamente");
			logger.info("Datos actualizados: {}", inventario);
			return new ResponseEntity<>(inventario, HttpStatus.OK);

		} catch (UsernameNotFoundException e) {
			logger.error("Error: Usuario no encontrado", e);
			return new ResponseEntity<>("Usuario no encontrado", HttpStatus.UNAUTHORIZED); // 401

		} catch (AccessDeniedException e) {
			logger.error("Error: Acceso denegado", e);
			return new ResponseEntity<>("No tienes permisos para realizar esta acción", HttpStatus.FORBIDDEN); // 403

		} catch (Exception e) {
			logger.error("Error desconocido al editar inventario", e);
			return new ResponseEntity<>("Error en el servidor", HttpStatus.INTERNAL_SERVER_ERROR); // 500
		}

	}

	@DeleteMapping("/delete/inventario/{id}")
	public ResponseEntity<?> deleteInventario(@PathVariable("id") int id, Authentication authentication) {

		try {
			// Obtener usuario autenticado
			String username = authentication.getName();

			Optional<Usuario> optionalUsuario = usuarioService.obtenerUsuario(username);

			if (optionalUsuario.isEmpty()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Collections.singletonMap("message", "Usuario no encontrado"));
			}

			Usuario usuario = optionalUsuario.get(); // Obtenemos al usuario si esta presente y validando no solo la
														// authenticacion sino tambien la verificacion de que el usuario
														// esta registrado en la bd

			Inventario inventario = inventarioService.getOne(id)
					.orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

			inventarioService.deleteInventarioByUsuario(id, usuario.getId());

			// Con esto eliminamos a la zapatilla asociada
			if (inventario.getZapatilla() != null) {
				zapatillaService.delete(inventario.getZapatilla().getZapatilla_id());
			}

			logger.info("Inventario eliminado");
			return new ResponseEntity<>("Inventario eliminado", HttpStatus.OK);

		} catch (AccessDeniedException e) {
			logger.error("Error: Acceso denegado", e);
			return new ResponseEntity<>("No tienes permisos para realizar esta acción", HttpStatus.FORBIDDEN); // 403

		} catch (Exception e) {
			logger.error("Error desconocido al eliminar inventario", e);
			return new ResponseEntity<>("Error en el servidor", HttpStatus.INTERNAL_SERVER_ERROR); // 500
		}

	}

	// Buscar por marca de la zapatilla desde inventario -- ROLE_ADMIN y ROLE_USER
	@GetMapping("/find/marca-zapatilla/{marca}")
	public ResponseEntity<?> buscarPorMarca(@PathVariable("marca") String marca, Authentication authentication) {

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

			List<Inventario> listInventario = inventarioService.buscarZapatillaPorMarcaYUsuario(marca, usuario.getId());

			if (listInventario.isEmpty()) {
				return new ResponseEntity<>("Marca no encontrada", HttpStatus.BAD_REQUEST);
			}
			logger.info("Inventarios encontrados por marca OK!");
			return new ResponseEntity<List<Inventario>>(listInventario, HttpStatus.OK);

		} catch (UsernameNotFoundException e) {
			logger.error("Error: Usuario no encontrado", e);
			return new ResponseEntity<>("Usuario no encontrado", HttpStatus.UNAUTHORIZED); // 401

		} catch (AccessDeniedException e) {
			logger.error("Error: Acceso denegado", e);
			return new ResponseEntity<>("No tienes permisos para realizar esta acción", HttpStatus.FORBIDDEN); // 403

		} catch (Exception e) {
			logger.error("Error desconocido al editar inventario", e);
			return new ResponseEntity<>("Error en el servidor", HttpStatus.INTERNAL_SERVER_ERROR); // 500
		}
	}
	
	//Método para ordenar inventario por precio descendente
	@GetMapping("/find/precio/orden")
	public ResponseEntity<?> orderInvetarioByPrecio (@RequestParam ("orden") String orden,
													 Authentication authentication){
		
		try {
			
			String username = authentication.getName();
			
			Optional<Usuario> optionalUsuario = usuarioService.obtenerUsuario(username);
			
			if (optionalUsuario.isEmpty()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Collections.singletonMap("message", "Usuario no encontrado"));
			}
			
			Usuario usuario = optionalUsuario.get();
			
			List<Inventario> listInventario = inventarioService.orderInventarioByPrecio(usuario.getId(), orden);
			
			if (listInventario.isEmpty()) {
				return new ResponseEntity<>(Map.of("mensaje", "No se encontró inventarios"), HttpStatus.BAD_REQUEST);
			}
			
			logger.info("Inventario ordenado por precio OK !");
			return new ResponseEntity<>(listInventario, HttpStatus.OK);
			
		} catch (UsernameNotFoundException e) {
			logger.error("Error: Usuario no encontrado", e);
			return new ResponseEntity<>("Usuario no encontrado", HttpStatus.UNAUTHORIZED); // 401

		} catch (AccessDeniedException e) {
			logger.error("Error: Acceso denegado", e);
			return new ResponseEntity<>("No tienes permisos para realizar esta acción", HttpStatus.FORBIDDEN); // 403

		} catch (Exception e) {
			logger.error("Error desconocido al editar inventario", e);
			return new ResponseEntity<>("Error en el servidor", HttpStatus.INTERNAL_SERVER_ERROR); // 500
		}
	}
	
	//Método para filtrar inventario entre fechas
	@GetMapping("/find/fecha/rango")
	public ResponseEntity<?> getInventarioByFechaCompraBetween(@RequestParam ("fechaInicio") String fechaInicio,
															   @RequestParam ("fechaFin") String fechaFin,
															   Authentication authentication){
		
		try {
			
			String username = authentication.getName();
			
			Optional<Usuario> optionalUsuario = usuarioService.obtenerUsuario(username);
			
			if (optionalUsuario.isEmpty()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Collections.singletonMap("message", "Usuario no encontrado"));
			}
			
			Usuario usuario = optionalUsuario.get();
			
			if (fechaInicio == null || fechaFin == null) {
				return new ResponseEntity<>(Map.of("error", "Por favor ingresar ambas fechas"), HttpStatus.BAD_REQUEST);
			}
			
			List<Inventario> listInventario = inventarioService.findInventarioByFechaCompraBetween(fechaInicio, fechaFin, usuario.getId());
			
			if (listInventario.isEmpty()) {
				return new ResponseEntity<>(Map.of("detalle", "No se encontraron inventarios entre el rango de fechas"), HttpStatus.NOT_FOUND);
			}
			
			logger.info("Filtro de inventario entre fechas OK !");
			return new ResponseEntity<>(listInventario, HttpStatus.OK);
			
		} catch (UsernameNotFoundException e) {
			logger.error("Error: Usuario no encontrado", e);
			return new ResponseEntity<>("Usuario no encontrado", HttpStatus.UNAUTHORIZED); // 401

		} catch (AccessDeniedException e) {
			logger.error("Error: Acceso denegado", e);
			return new ResponseEntity<>("No tienes permisos para realizar esta acción", HttpStatus.FORBIDDEN); // 403

		} catch (Exception e) {
			logger.error("Error desconocido al editar inventario", e);
			return new ResponseEntity<>("Error en el servidor", HttpStatus.INTERNAL_SERVER_ERROR); // 500
		}
	}
	
	/*
	//Método para buscar inventario por fecha_compra (año)
	//Este método no esta utilizando en el frontend
	@GetMapping("/find/fecha/{anio}")
	public ResponseEntity<?> getInventarioByFechaCompraAnio(@PathVariable("anio") int anio, Authentication authentication){
		
		try {
			
			String username = authentication.getName();
			
			Optional<Usuario> optionalUsuario = usuarioService.obtenerUsuario(username);
			
			if (optionalUsuario.isEmpty()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Collections.singletonMap("message", "Usuario no encontrado"));
			}
			
			Usuario usuario = optionalUsuario.get();
			
			List<Inventario> listInventario = inventarioService.findInventarioByFechaAnio(anio, usuario.getId());
			
			if (listInventario.isEmpty()) {
				return new ResponseEntity<>(Map.of("message","No hay inventario con ese año de fecha"), HttpStatus.BAD_REQUEST);
			}
			
			logger.info("Inventarios encontrados por año OK!");
			return new ResponseEntity<>(listInventario, HttpStatus.OK);			
			
		} catch (UsernameNotFoundException e) {
			logger.error("Error: Usuario no encontrado", e);
			return new ResponseEntity<>("Usuario no encontrado", HttpStatus.UNAUTHORIZED); // 401

		} catch (AccessDeniedException e) {
			logger.error("Error: Acceso denegado", e);
			return new ResponseEntity<>("No tienes permisos para realizar esta acción", HttpStatus.FORBIDDEN); // 403

		} catch (Exception e) {
			logger.error("Error desconocido al editar inventario", e);
			return new ResponseEntity<>("Error en el servidor", HttpStatus.INTERNAL_SERVER_ERROR); // 500
		}
	}
	*/

	// Método para obtener la cantidad de inventario que tiene un usuario
	@GetMapping("/cantidad-inventario-usuario")
	public ResponseEntity<?> getCantidadInventarioByUsuario(Authentication authentication) {

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

			int cantidadInventarioDeUsuario = inventarioService.countInventarioByUsuarioId(usuario.getId());

			// Este if es opcional, se puede retirar tambien
			if (cantidadInventarioDeUsuario == 0) {
				return new ResponseEntity<>("0", HttpStatus.OK);
			}

			logger.info("La cantidad de inventario del usuario es:" + cantidadInventarioDeUsuario);
			return ResponseEntity.ok(Collections.singletonMap("cantidad_inventario", cantidadInventarioDeUsuario));
			// return new ResponseEntity<>(cantidadInventarioDeUsuario,
			// HttpStatus.OK);//Esto solo manda la cantidad json plano

		} catch (UsernameNotFoundException e) {
			logger.error("Error: Usuario no encontrado", e);
			return new ResponseEntity<>("Usuario no encontrado", HttpStatus.UNAUTHORIZED); // 401

		} catch (AccessDeniedException e) {
			logger.error("Error: Acceso denegado", e);
			return new ResponseEntity<>("No tienes permisos para realizar esta acción", HttpStatus.FORBIDDEN); // 403

		} catch (Exception e) {
			logger.error("Error desconocido al editar inventario", e);
			return new ResponseEntity<>("Error en el servidor", HttpStatus.INTERNAL_SERVER_ERROR); // 500
		}
	}

	@GetMapping("/marca-silueta-zapatilla-inventario")
	public ResponseEntity<?> getMarcaSiluetaLastZapatilla(Authentication authentication) {

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

			String marcaSiluetaZapatilla = inventarioService.fidMarcaAndSiluetaByLastZapatilla(usuario.getId());
			
			if (marcaSiluetaZapatilla == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("message", "No se encontró una zapatilla en el inventario"));
            }

			logger.info("La marca y silueta de la ultima zapatilla registrada en el inventario es: " + marcaSiluetaZapatilla);
			return ResponseEntity.ok(Collections.singletonMap("marca_silueta", marcaSiluetaZapatilla));
			
		} catch (UsernameNotFoundException e) {
			logger.error("Error: Usuario no encontrado", e);
			return new ResponseEntity<>("Usuario no encontrado", HttpStatus.UNAUTHORIZED); // 401

		} catch (AccessDeniedException e) {
			logger.error("Error: Acceso denegado", e);
			return new ResponseEntity<>("No tienes permisos para realizar esta acción", HttpStatus.FORBIDDEN); // 403

		} catch (Exception e) {
			logger.error("Error desconocido al encontar la marca y silueta de la zapatilla en el  inventario", e);
			return new ResponseEntity<>("Error en el servidor", HttpStatus.INTERNAL_SERVER_ERROR); // 500
		}

	}
	
	//Este método es para obtener el monto total gastado de zapatillas por usuario
	@GetMapping("/obtener-monto-total-gastado")
	public ResponseEntity<?> getMontoTotalGastadoZapatillas (Authentication authentication){
		
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
			
			double montoTotal = inventarioService.findMontoTotalZapatilla(usuario.getId());
			
			logger.info("El monto total es: ",montoTotal);
			return ResponseEntity.ok(Collections.singletonMap("monto_total", montoTotal));
			
		} catch (UsernameNotFoundException e) {
			logger.error("Error: Usuario no encontrado", e);
			return new ResponseEntity<>("Usuario no encontrado", HttpStatus.UNAUTHORIZED); // 401

		} catch (AccessDeniedException e) {
			logger.error("Error: Acceso denegado", e);
			return new ResponseEntity<>("No tienes permisos para realizar esta acción", HttpStatus.FORBIDDEN); // 403

		} catch (Exception e) {
			logger.error("Error al obtener el monto total gastado en zapatillas", e);
			return new ResponseEntity<>("Error en el servidor", HttpStatus.INTERNAL_SERVER_ERROR); // 500
		}
	}

	//Este método es para obtener la mayor marca y cuantos pares de zapatillas compradas por usuario
	@GetMapping("/marca-cantidad-zapatilla-mayor-comprada")
	public ResponseEntity<?> getMarcaCantidadZatillaMasComprada(Authentication authentication){
		
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
			
			Map<String, Long> resultado = inventarioService.obtenerMarcaMasComprada(usuario.getId());
			
			if (resultado.isEmpty()) {
				return new ResponseEntity<>("No hay zapatilla", HttpStatus.NO_CONTENT);
			}
			
			// Transformamos el resultado para que nos muestre en json ambos valores
			Map<String, Object> respuesta = new HashMap<>();
			resultado.forEach((marca, cantidad) -> {
			    respuesta.put("Marca", marca);
			    respuesta.put("Cantidad", cantidad);
			});
			
			logger.info("La marca mas comprada es: ",respuesta);
			return ResponseEntity.ok(respuesta);
			
		} catch (UsernameNotFoundException e) {
			logger.error("Error: Usuario no encontrado", e);
			return new ResponseEntity<>("Usuario no encontrado", HttpStatus.UNAUTHORIZED); // 401

		} catch (AccessDeniedException e) {
			logger.error("Error: Acceso denegado", e);
			return new ResponseEntity<>("No tienes permisos para realizar esta acción", HttpStatus.FORBIDDEN); // 403

		} catch (Exception e) {
			logger.error("Error al obtener la marca y la cantidad de la zapatilla mas comprada por usuario", e);
			return new ResponseEntity<>("Error en el servidor", HttpStatus.INTERNAL_SERVER_ERROR); // 500
		}
	}
	// Listar con paginacion -- ROLE_ADMIN --En el frontend se esta utlizando este método
		@GetMapping("/list/zapatillas")
		public ResponseEntity<Page<Zapatilla>> findPageZapatillas(@RequestParam int page, @RequestParam int size) {
			Page<Zapatilla> listPageZapatillas = zapatillaService.listPageZapatillas(page, size);
			listPageZapatillas.forEach(imagen -> {
				String nombreImagen = imagen.getImagen();// Asumiendo que es el nombre de la imagen
				imagen.setImagen("/api-inventario/imagen-zapatilla/" + nombreImagen);
			});
			return new ResponseEntity<>(listPageZapatillas, HttpStatus.OK);
		}
		
	// Zapatillas CRUD
	// Listar sin paginacion -- ROLE_ADMIN
	@GetMapping("/list-all/zapatillas")
	public ResponseEntity<List<Zapatilla>> findAllZapatillas() {
		List<Zapatilla> listAllZapatillas = zapatillaService.listZapatillas();
		return new ResponseEntity<>(listAllZapatillas, HttpStatus.OK);
	}
	
	//Estado CRUD
	@GetMapping("/list-all/estado")
	public ResponseEntity<?> getAllEstados(){
		
		try {
			List<Estado> listEstados = estadoService.listAllEstado();
			logger.info("Listando todos los estados", listEstados);
			return new ResponseEntity<>(listEstados, HttpStatus.OK);
			
		} catch (Exception e) {
			logger.error("Error al listar los estados", e.getMessage());
			return new ResponseEntity<>("Error al listar los estados", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	//Método para buscar zapatillas por la marca, este endpoint es para el user con ROLE_ADMIN
	@GetMapping("/find/zapatilla-marca-admin/{marca}")
	public ResponseEntity<?> getZapatillasByMarca(@PathVariable("marca") String marca,
												  Authentication authentication){
		
		try {
			String username = authentication.getName();

			Optional<Usuario> optionalUsuario = usuarioService.obtenerUsuario(username);

			if (optionalUsuario.isEmpty()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body(Collections.singletonMap("message", "Usuario no encontrado"));
			}

			List<Zapatilla> listZapatillas = zapatillaService.findZapatillaByMarca(marca);
			logger.info("Zapatillas encontradas por la marca del metodo getZapatillasByMarca");
			return new ResponseEntity<List<Zapatilla>>(listZapatillas, HttpStatus.OK);
			
		} catch (UsernameNotFoundException e) {
			logger.error("Error: Usuario no encontrado", e);
			return new ResponseEntity<>("Usuario no encontrado", HttpStatus.UNAUTHORIZED); // 401

		} catch (AccessDeniedException e) {
			logger.error("Error: Acceso denegado", e);
			return new ResponseEntity<>("No tienes permisos para realizar esta acción", HttpStatus.FORBIDDEN); // 403

		} catch (Exception e) {
			logger.error("Error al obtener la zapatilla por la marca", e);
			return new ResponseEntity<>("Error en el servidor", HttpStatus.INTERNAL_SERVER_ERROR); // 500
		}
		
	}


	/*
	 * Los siguientes endpoints quedan como ejemplo de como guardar un objeto y un
	 * paremetro Multipart pero no se esta consumiendo en la aplicacion, porque una
	 * zapatilla se gestiona desde Inventario
	 */
	@PostMapping("/add/zapatillas")
	public ResponseEntity<?> addZapatillas(@ModelAttribute ZapatillaDTO zapatillaDTO,
			@RequestParam("imagen") MultipartFile imagen) {

		try {
			Zapatilla zapatilla = new Zapatilla(zapatillaDTO.getMarca(), 
					                            zapatillaDTO.getSilueta(),
					                            zapatillaDTO.getTalla(), 
					                            zapatillaDTO.getColorway(), 
					                            zapatillaDTO.getMateriales(), 
					                            null,
					                            zapatillaDTO.getColaboracion());// La
																												// imagen
																												// se
																												// asignara
																												// despues
																												// de
																												// subir
																												// a
																												// Cloudinary
			zapatillaService.save(zapatilla, imagen);
			return new ResponseEntity<>(zapatilla, HttpStatus.CREATED);

		} catch (Exception e) {
			return new ResponseEntity<>("Error al crear la zapatilla" + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/update/zapatillas/{id}")
	public ResponseEntity<?> updateZapatilla(@PathVariable("id") int id, @ModelAttribute ZapatillaDTO zapatillaDTO,
			@RequestParam("imagen") MultipartFile imagen) {
		try {
			Optional<Zapatilla> optionalZapatilla = zapatillaService.getOne(id);
			if (!optionalZapatilla.isPresent()) {
				return new ResponseEntity<>("Zapatilla no encontrada", HttpStatus.NO_CONTENT);
			}
			// Obtenemos la zapatilla existente
			Zapatilla zapatilla = optionalZapatilla.get();

			// Actualizamos la zapatilla
			zapatilla.setMarca(zapatillaDTO.getMarca());
			zapatilla.setSilueta(zapatillaDTO.getSilueta());
			zapatilla.setTalla(zapatillaDTO.getTalla());
			zapatilla.setColorway(zapatillaDTO.getColorway());
			zapatilla.setMateriales(zapatillaDTO.getMateriales());
			zapatilla.setColaboracion(zapatillaDTO.getColaboracion());

			zapatillaService.save(zapatilla, imagen);
			return new ResponseEntity<>("Zapatilla actualizada", HttpStatus.OK);

		} catch (Exception e) {
			return new ResponseEntity<>("ERROR AL ACTUALIZAR LA ZAPATILLA " + e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@DeleteMapping("/delete/zapatillas/{id}")
	public ResponseEntity<?> deleteZapatilla(@PathVariable("id") int id) {
		Zapatilla zapatilla = zapatillaService.findById(id);
		if (zapatilla == null) {
			return new ResponseEntity<>("ERROR, ZAPATILLA NO ENCONTRADA !!", HttpStatus.BAD_REQUEST);
		}
		zapatillaService.delete(id);
		return new ResponseEntity<>("Zapatilla Eliminada", HttpStatus.OK);
	}

	// Buscar o mostrar inventario por id
	@GetMapping("/find/inventario/{id}")
	public ResponseEntity<?> getInventarioById(@PathVariable("id") int id) {
		Inventario inventario = inventarioService.findById(id);
		if (inventario == null) {
			return new ResponseEntity<>("Inventario no encontrado", HttpStatus.BAD_REQUEST);
		}
		// Construyendo la URL para mostrar la imagen de la zapatilla
		String nombreImagenZapatilla = inventario.getZapatilla().getImagen();
		inventario.getZapatilla().setImagen("/api-inventario/imagen-zapatilla/" + nombreImagenZapatilla);
		return new ResponseEntity<>(inventario, HttpStatus.OK);
	}

}
