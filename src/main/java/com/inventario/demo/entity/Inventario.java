package com.inventario.demo.entity;

import java.time.LocalDate;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "inventario")
public class Inventario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "inventario_id")
	private int id;
	private int cantidad;
	private double precio;
	private String comentario;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate fecha_compra;
	//private Date fecha_compra;
	
	
	
	@ManyToOne(fetch = FetchType.EAGER, cascade=CascadeType.PERSIST)
	@JoinColumn(name = "zapatilla_id")
	private Zapatilla zapatilla;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "usuario_id")
	private Usuario usuario;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "estado_id")
	private Estado estado;

	public Inventario(int cantidad, double precio, LocalDate fecha_compra, String comentario, Zapatilla zapatilla,
			Usuario usuario, Estado estado) {
		super();
		this.cantidad = cantidad;
		this.precio = precio;
		this.fecha_compra = fecha_compra;
		this.comentario = comentario;
		this.zapatilla = zapatilla;
		this.usuario = usuario;
		this.zapatilla = zapatilla;
		this.estado = estado;
	}
	
}
