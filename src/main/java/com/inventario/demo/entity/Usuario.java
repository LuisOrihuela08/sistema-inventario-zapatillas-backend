package com.inventario.demo.entity;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.UniqueConstraint;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "usuario")
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "usuario_id")
	private int id;
	private String username;
	private String password;
	private String nombre;
	private String apellido;
	private String correo;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date fecha_nac;
		
	@Transient
    private boolean admin;
	
	@OneToMany(mappedBy = "usuario")
	@JsonIgnore
	private List<Inventario> inventario;
	
	@ManyToMany
	@JoinTable(name = "users_roles",
 		   	   joinColumns = {@JoinColumn(name = "usuario_id")},
 		       inverseJoinColumns = {@JoinColumn(name="rol_id")},
 		       uniqueConstraints = {@UniqueConstraint(columnNames = {"usuario_id","rol_id"})})
	//@JsonIgnore
	private List<Rol> rol;

	public Usuario(String username, String password, String nombre, String apellido, String correo, Date fecha_nac) {
		super();
		this.username = username;
		this.password = password;
		this.nombre = nombre;
		this.apellido = apellido;
		this.correo = correo;
		this.fecha_nac = fecha_nac;
	}
	/*
	//Método para formatear la fecha_nac
	public String getFechaNacimientoFormateada() {
		SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
		return formato.format(fecha_nac);
	}*/
	
	
}
