package com.inventario.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.inventario.demo.entity.Rol;

public interface RolRepository extends JpaRepository<Rol, Integer>{

	Optional<Rol> findByName (String name);

}
