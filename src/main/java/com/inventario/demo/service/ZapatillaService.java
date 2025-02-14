package com.inventario.demo.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.inventario.demo.controller.InventarioController;
import com.inventario.demo.entity.Zapatilla;
import com.inventario.demo.repository.ZapatillaRepository;

@Service
public class ZapatillaService {
	
	private final Logger logger = LoggerFactory.getLogger(ZapatillaService.class);

	@Autowired
	private ZapatillaRepository zapatillaRepository;
	
	//Aca inyectamos las credenciales para acceder a Cloudinary
	@Value("${cloudinary.cloud_name}")
	private String cloudName;
	
	@Value("${cloudinary.api_key}")
	private String apiKey;

	@Value("${cloudinary.api_secret}")
	private String apiSecret;
	
	private Cloudinary cloudinary;
	
	//Aca inicializamos a Cloudinary con nuestras credenciales
	public void initializerCloudinary() {
		cloudinary = new Cloudinary(ObjectUtils.asMap("cloud_name", cloudName,
													  "api_key", apiKey,
													  "api_secret", apiSecret));
	}
	
	//Este método es solo para la imagen de la zapatilla y se utilizara para guardar desde el Inventario
	public String guardarImagen(MultipartFile imagen) throws IOException{
		initializerCloudinary(); // Inicializamos Cloudinary
		
		// Subimos la imagen a Cloudinary
	    Map<String, Object> uploadCloudinary = cloudinary.uploader().upload(imagen.getBytes(), ObjectUtils.asMap(
	        "folder", "zapatillas-inventario",
	        "public_id", imagen.getOriginalFilename().replaceAll("[^a-zA-Z0-9.\\-_]", "_")
	    ));

	 // Retornamos la URL de la imagen subida
	    return uploadCloudinary.get("url").toString();
	}
	
	
	
	//Listar sin paginacion
	public List<Zapatilla> listZapatillas(){
		return zapatillaRepository.findAll();
	}
	
	//Lista con paginacion
	public Page<Zapatilla> listPageZapatillas(int page, int size){
		Pageable pageable = PageRequest.of(page, size);
		return zapatillaRepository.findAll(pageable);
	}
	
	public void eliminarImagen(String imagenURL) {
	    if (imagenURL == null || imagenURL.isEmpty()) {
	        logger.warn("No se proporcionó una URL de imagen para eliminar.");
	        return;
	    }

	    try {
	        // Extraer el public_id de la URL
	        String publicId = extractPublicIdFromUrl(imagenURL);
	        
	        // Eliminar la imagen de Cloudinary
	        deleteImageFromCloudinary(publicId);

	        logger.info("Imagen eliminada correctamente: " + imagenURL);
	    } catch (IOException e) {
	        logger.error("Error al eliminar la imagen en Cloudinary: " + imagenURL, e);
	    } catch (Exception e) {
	        logger.error("Error inesperado al eliminar la imagen: " + imagenURL, e);
	    }
	}

	
	/*
	 * Este metodo no se esta consumiendo para la aplicacion, pero lo dejare aqui como ejemplo
	 * de como guardar un objeto con un atributo o parametro Multipart, en este caso una imagen
	 */
	public void save(Zapatilla zapatilla, MultipartFile imagen) throws IOException { 
	    initializerCloudinary(); // Inicializamos Cloudinary

	    // Subimos la imagen a Cloudinary
	    Map<String, Object> uploadCloudinary = cloudinary.uploader().upload(imagen.getBytes(), ObjectUtils.asMap(
	        "folder", "zapatillas-inventario",
	        "public_id", imagen.getOriginalFilename().replaceAll("[^a-zA-Z0-9.\\-_]", "_")
	    ));

	    // Obtenemos la URL de la imagen subida
	    String imagenURL = uploadCloudinary.get("url").toString();

	    // Asignamos la URL de la imagen al objeto Zapatilla
	    zapatilla.setImagen(imagenURL);

	    // Guardamos en la base de datos
	    zapatillaRepository.save(zapatilla);
	}
		
	public Optional<Zapatilla> getOne (int id){
		return zapatillaRepository.findById(id);
	}
	
	public void delete (int id) {
		zapatillaRepository.deleteById(id);
	}
	
	//Buscar por id
	public Zapatilla findById (int id) {
		Optional<Zapatilla> optionalZapatilla = zapatillaRepository.findById(id);
		return optionalZapatilla.orElse(null);
	}
	//Este método es para buscar por marca
	public List<Zapatilla> findZapatillaByMarca(String marca){
		return zapatillaRepository.findByMarca(marca);
	}
		
	public void updateZapatilla(Zapatilla zapatilla, MultipartFile imagen) throws IOException {
	    initializerCloudinary(); // Inicializamos Cloudinary

	    // Verificamos si la imagen fue actualizada
	    if (imagen != null && !imagen.isEmpty()) {
	        // Si ya existe una imagen en la entidad, la eliminamos de Cloudinary
	        if (zapatilla.getImagen() != null) {
	            String publicId = extractPublicIdFromUrl(zapatilla.getImagen()); // Extraemos el public_id
	            deleteImageFromCloudinary(publicId); // Eliminamos la imagen de Cloudinary
	        }

	        // Subimos la nueva imagen a Cloudinary
	        Map<String, Object> uploadCloudinary = cloudinary.uploader().upload(imagen.getBytes(), 
	            ObjectUtils.asMap(
	                "folder", "zapatillas-inventario",
	                "public_id", imagen.getOriginalFilename().replaceAll("[^a-zA-Z0-9.\\-_]", "_")
	            ));

	        // Obtenemos la URL de la imagen subida
	        String imagenURL = uploadCloudinary.get("url").toString();
	        zapatilla.setImagen(imagenURL); // Asignamos la URL de la imagen al objeto Zapatilla
	    }

	    // Guardamos la zapatilla actualizada
	    zapatillaRepository.save(zapatilla);
	}

	// Método para extraer el public_id de la URL de la imagen
	private String extractPublicIdFromUrl(String imageUrl) {
	    // Suponiendo que la URL tiene el formato adecuado, extraemos el public_id
	    String[] parts = imageUrl.split("/"); // Spliteamos la URL por "/"
	    String publicIdWithExtension = parts[parts.length - 1];
	    return publicIdWithExtension.split("\\.")[0]; // El public_id no tiene la extensión
	}
	
	//Método para eliminar una imagen en cloudinary
	public void deleteImageFromCloudinary(String publicId) throws IOException {
	    initializerCloudinary(); // Inicializamos Cloudinary

	    // Eliminamos la imagen de Cloudinary usando el public_id
	    Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());

	    // Imprimir resultado para depuración (opcional)
	    System.out.println("Imagen eliminada de Cloudinary: " + result);
	}


}
