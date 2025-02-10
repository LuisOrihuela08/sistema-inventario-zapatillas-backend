package com.inventario.demo.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.inventario.demo.entity.Inventario;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Integer>{

	//Esto es para buscar un inventario por la marca de una zapatilla
	List<Inventario> findByZapatillaMarcaAndUsuarioId (String marca, int usuarioId);
	
	//Esto es para eliminar un inventario de un usuario
	void deleteInventarioByIdAndUsuarioId(int inventario_id, int usuario_id);
	
	Page<Inventario> findAll (Pageable pageable);
	
	//Este método es para listar los inventarios con paginacion pero por usuario
	Page<Inventario> findAllInventarioByUsuario_Id(Pageable pageable, int usuario_id);
	
	//Esto es para buscar el inventario por usuario
	List<Inventario> findByUsuario_Id (int usuarioId);
	
	//Esto es para obtener la cantidad de inventario tiene un usuario
	@Query("SELECT COUNT(i) FROM Inventario i WHERE i.usuario.id = :usuario_id")
    int countInventarioByUsuarioId(@Param("usuario_id") int usuario_id);
	
	//Esto es para obtener la marca y silueta de la ultima zapatilla agregada al inventario
	@Query("""
		    SELECT CONCAT(z.marca, ' ', z.silueta) 
		    FROM Inventario i
		    JOIN Zapatilla z ON i.zapatilla.id = z.id
		    WHERE i.id = (SELECT MAX(i2.id) FROM Inventario i2 WHERE i2.usuario.id = :usuario_id)
		""")
	 String findUltimaZapatilla(@Param("usuario_id") int usuario_id);
	
	//Esto es para obtener el monto total gastado en zapatillas de un usuario
	@Query("""
			SELECT SUM(i.precio) 
			FROM Inventario i
			JOIN Usuario u ON i.usuario.id = u.id
			WHERE i.usuario.id = :usuario_id
			""")
	double obtenerMontoTotalGastado(@Param("usuario_id") int usuario_id);
	
	//Obtener la marca de la zapatilla mas comprada y su cantidad por usuario
	@Query("""
		    SELECT z.marca, COUNT(z.marca) AS cantidadComprada
		    FROM Inventario i
		    JOIN i.zapatilla z
		    WHERE i.usuario.id = :usuario_id
		    GROUP BY z.marca
		    ORDER BY COUNT(z.marca) DESC
		""")
		List<Object[]> obtenerMarcaMasCompradaPorUsuario(@Param("usuario_id") int usuario_id, Pageable pageable);

	
}
