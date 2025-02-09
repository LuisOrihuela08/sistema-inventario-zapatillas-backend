package com.inventario.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.inventario.demo.entity.Estado;
import com.inventario.demo.repository.EstadoRepository;

@Service
public class EstadoService {

	@Autowired
	private EstadoRepository estadoRepository;
	
	public List<Estado> listAllEstado(){
		return estadoRepository.findAll();
	}
	
	public Optional<Estado> findById(int id){
		return estadoRepository.findById(id);
	}
}
