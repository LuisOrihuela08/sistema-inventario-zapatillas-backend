package com.inventario.demo.dto;

import org.springframework.web.multipart.MultipartFile;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ZapatillaDTO {

	private String marca;
	private String silueta;
	private double talla;
	private String colorway;
	private String materiales;
	private MultipartFile imagen;
	
}
