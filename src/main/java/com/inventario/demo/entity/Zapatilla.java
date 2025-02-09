package com.inventario.demo.entity;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "zapatilla")
public class Zapatilla {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int zapatilla_id;
	private String marca;
	private String silueta;
	private double talla;
	private String colorway;
	private String materiales;
	private String imagen;
	private String colaboracion;
	
	@OneToMany(mappedBy = "zapatilla")
	@JsonIgnore
	private List<Inventario> inventario;

	public Zapatilla(String marca, String silueta, double talla, String colorway, String materiales, String imagen, String colaboracion) {
		super();
		this.marca = marca;
		this.silueta = silueta;
		this.talla = talla;
		this.colorway = colorway;
		this.materiales = materiales;
		this.imagen = imagen;
		this.colaboracion = colaboracion;
	}
	
}
