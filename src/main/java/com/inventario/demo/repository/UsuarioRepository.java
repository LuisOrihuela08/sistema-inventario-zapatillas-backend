package com.inventario.demo.repository;


import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.inventario.demo.entity.Usuario;

import jakarta.transaction.Transactional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer>{

	Optional<Usuario>  findByUsername(String username);
	
	Page<Usuario> findAll(Pageable pageable);
	
	//Método para eliminar un usuario y su llave foranea en rol_usuario
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM users_roles WHERE usuario_id = :usuario_id", nativeQuery = true)
	void deleteRolesByUserId(@Param("usuario_id") int usuario_id);
}
