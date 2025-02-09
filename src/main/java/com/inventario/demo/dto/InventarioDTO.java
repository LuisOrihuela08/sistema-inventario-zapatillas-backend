package com.inventario.demo.dto;

import java.util.Date;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.inventario.demo.entity.Estado;
import com.inventario.demo.entity.Usuario;
import com.inventario.demo.entity.Zapatilla;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class InventarioDTO {

	private int cantidad;
	private double precio;
	private String comentario;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date fecha_compra;
	
	// Datos de la zapatilla
    private String marca;
    private String silueta;
    private double talla;
    private String colorway;
    private String materiales;

    // Imagen de la zapatilla
    private MultipartFile imagen;
    
    //Atributo de Estado
    private int estado_id; 

}
