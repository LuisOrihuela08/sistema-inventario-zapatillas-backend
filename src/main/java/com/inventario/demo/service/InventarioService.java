package com.inventario.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inventario.demo.entity.Inventario;
import com.inventario.demo.entity.Usuario;
import com.inventario.demo.repository.InventarioRepository;

@Service
public class InventarioService {

	@Autowired
	private InventarioRepository inventarioRepository;

	// Listar sin paginacion
	public List<Inventario> listInventario() {
		return inventarioRepository.findAll();
	}

	// Listar con paginacion
	public Page<Inventario> listPageInventario(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return inventarioRepository.findAll(pageable);
	}
	
	//Listar inventarios con paginacion pero por usuario
	public Page<Inventario> listInventarioPageByUsuario(int page, int size, int usuario_id, String orden){
		//Esto es para ordenar tambien por precio
		Sort sort = Sort.by("precio");
		
		if ("desc".equalsIgnoreCase(orden)) {
			sort = sort.descending();
		} else {
			sort = sort.ascending();
		}
		
		Pageable pageable = PageRequest.of(page, size, sort);
		return inventarioRepository.findAllInventarioByUsuario_Id(pageable, usuario_id);
	}

	public void save(Inventario inventario) {
		inventarioRepository.save(inventario);
	}

	public Optional<Inventario> getOne(int id) {
		return inventarioRepository.findById(id);
	}

	// Método para eliminar un inventario verificando que es de un usuario en
	// especifico
	@Transactional // eso es para métodos de servicio que realizan operaciones de base de datos
	public void deleteInventarioByUsuario(int inventario_id, int usuario_id) {
		inventarioRepository.deleteInventarioByIdAndUsuarioId(inventario_id, usuario_id);
	}

	// Método para buscar por marca de la zapatilla desde el inventario
	public List<Inventario> buscarZapatillaPorMarcaYUsuario(String marca, int usuarioId) {
		return inventarioRepository.findByZapatillaMarcaAndUsuarioId(marca, usuarioId);
	}
	
	//Método para filtrar inventario entre fechas
	public List<Inventario> findInventarioByFechaCompraBetween (String fechaInicioString, String fechaFinString, int usuarioId){
		LocalDate fechaInicio = LocalDate.parse(fechaInicioString);
		LocalDate fechaFin = LocalDate.parse(fechaFinString);//Dejandolo asi, toma hasta la fecha fin que se le asigne, es decir: fechaInicio <= fecha_compra <= fechaFin
		
		//Realizamos la consulta al repo
		return inventarioRepository.buscarPorFechasYUsuario(fechaInicio, fechaFin, usuarioId);
		
	}
	
	//Método para ordenar inventarios por precio
	public List<Inventario> orderInventarioByPrecio(int usuarioId, String orden){
			
		Sort sort = orden.equalsIgnoreCase("desc") ? Sort.by("precio").descending() : Sort.by("precio").ascending();
		return inventarioRepository.findInventarioByUsuarioId(usuarioId, sort);
	}
	/*
	//Método para buscar por fecha (año) un inventario
	public List<Inventario> findInventarioByFechaAnio(int anio, int usuarioId){
		return inventarioRepository.findByFechaCompra(anio, usuarioId);
	}
	*/
	// Método para contar los inventario que tiene un usuario
	public int countInventarioByUsuarioId(int usuario_id) {
		return inventarioRepository.countInventarioByUsuarioId(usuario_id);
	}

	// Buscar inventario por usuario_id
	public List<Inventario> findInventarioByUsuarioId(int usuarioId) {
		return inventarioRepository.findByUsuario_Id(usuarioId);

	}
	
	//Método para obtener la marca y silueta de la ultima zapatilla agregada al inventario
	public String fidMarcaAndSiluetaByLastZapatilla(int usuario_id) {
		return inventarioRepository.findUltimaZapatilla(usuario_id);
	}

	//Método para obtener le monto total gastado en zapatillas de un usuario
	public double findMontoTotalZapatilla(int usuario_id) {
		return inventarioRepository.obtenerMontoTotalGastado(usuario_id);
	}
	
	//Método para obtener la marca mas comprado y cuanto por usario
	public Map<String, Long> obtenerMarcaMasComprada(int usuario_id) {
        Pageable pageable = PageRequest.of(0, 1); // Solo obtenemos la marca más comprada (top 1)
        List<Object[]> resultados = inventarioRepository.obtenerMarcaMasCompradaPorUsuario(usuario_id, pageable);

        Map<String, Long> resultadoMap = new HashMap<>();
        if (!resultados.isEmpty()) {
            Object[] fila = resultados.get(0);
            resultadoMap.put((String) fila[0], ((Number) fila[1]).longValue());
        }
        return resultadoMap;
    }
	
	// Método para buscar por id
	public Inventario findById(int id) {
		Optional<Inventario> optionalInventario = inventarioRepository.findById(id);
		return optionalInventario.orElse(null);
	}

	public void delete(int id) {
		inventarioRepository.deleteById(id);
	}

}
