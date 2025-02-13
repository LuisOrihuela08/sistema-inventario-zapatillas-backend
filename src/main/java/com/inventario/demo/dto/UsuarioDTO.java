package com.inventario.demo.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.inventario.demo.entity.Usuario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UsuarioDTO {

	private String username;
	private String password;
	private String nombre;
	private String apellido;
	private String correo;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date fecha_nac;
	private boolean admin;
	//private int id;//esto es solo para el metodo de editar perfil
	
	public UsuarioDTO(Usuario usuario) {
		//this.id = usuario.getId();
        this.username = usuario.getUsername();
        this.password = usuario.getPassword();//Eso lo comento porque no quiero que se muestre porque se ve encriptado
        this.nombre = usuario.getNombre();
        this.apellido = usuario.getApellido();
        this.correo = usuario.getCorreo();
        this.fecha_nac = usuario.getFecha_nac();
    }
}
