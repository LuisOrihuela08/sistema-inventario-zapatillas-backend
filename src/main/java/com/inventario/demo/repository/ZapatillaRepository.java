package com.inventario.demo.repository;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.inventario.demo.entity.Zapatilla;

public interface ZapatillaRepository extends JpaRepository<Zapatilla, Integer>{

	Page<Zapatilla> findAll (Pageable pageable);
	
	//Buscar zapatilla por marca
	List<Zapatilla> findByMarca(String marca);
}
