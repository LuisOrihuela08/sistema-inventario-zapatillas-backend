# sistema-inventario-zapatillas-backend

## Descripción
Este repositorio contendrá todo el código del backend para el sistema de inventario de zapatillas, donde cada usuario podra gestionar su propio inventario. El usuario con el ROLE_ADMIN, tendra más opciones y permisos a diferencia de usuario con el ROLE_USER.  
El backend está desarrollado en **Spring Boot** y utiliza **PostgreSQL** como base de datos, la cual está gestionada mediante un contenedor de **Docker**. Para la gestión de la base de datos se utiliza **DataGrip**.

## Tecnologías Utilizadas
- **Spring Boot 3.4.0**
- **Spring Security**
- **Java 17**
- **PostgreSQL**
- **Docker**
- **DataGrip**
- **JWT para autenticación y seguridad**
- **Cloudinary para gestión de imágenes**

## Diagrama de Base de Datos
El sistema cuenta con las siguientes entidades:
[![bd.png](https://i.postimg.cc/0Nkrtczt/bd.png)](https://postimg.cc/SnPyRL78)

### Usuario
Representa a los usuarios del sistema, quienes pueden registrar su propio inventario de zapatillas.
- `usuario_id` (PK) - Identificador único del usuario
- `user` - Nombre de usuario
- `password` - Contraseña
- `nombre` - Nombre del usuario
- `apellido` - Apellido del usuario
- `correo` - Correo electrónico del usuario

### Rol
Define los roles de los usuarios en el sistema.
- `rol_id` (PK) - Identificador único del rol
- `nombre` - Nombre del rol (ejemplo: ROLE_ADMIN, ROLE_USER)

### Usuario_Rol
Relación entre usuarios y roles.
- `usuario_rol_id` (PK) - Identificador único
- `usuario_id` (FK) - Referencia a la tabla Usuario
- `rol_id` (FK) - Referencia a la tabla Rol

### Zapatilla
Almacena la información de cada zapatilla registrada en el sistema.
- `zapatilla_id` (PK) - Identificador único de la zapatilla
- `marca` - Marca de la zapatilla
- `silueta` - Modelo de la zapatilla
- `colaboracion` - Marca colaborativa con la marca
- `talla` - Talla de la zapatilla
- `colorway` - Color de la zapatilla
- `materiales` - Materiales de fabricación
- `imagen` - URL de la imagen almacenada en Cloudinary

### Estado
Define los roles de los usuarios en el sistema.
- `estado_id` (PK) - Identificador único del estado
- `nombreEstado` - Nombre del estado (ejemplo: En uso, Desuso, Vendido)

### Inventario
Relaciona las zapatillas con los usuarios que las poseen.
- `inventario_id` (PK) - Identificador único del inventario
- `cantidad` - Cantidad de pares de esa zapatilla en el inventario
- `fecha_compra` - Fecha en la que se adquirió la zapatilla
- `precio` - Precio de compra
- `comentario` - Notas adicionales
- `zapatilla_id` (FK) - Referencia a la zapatilla en el inventario
- `usuario_id` (FK) - Referencia al usuario propietario

## Configuración del Proyecto

### Requisitos Previos
1. **Docker** instalado y en ejecución.
2. **PostgreSQL** configurado en un contenedor Docker.
3. **Java 17** instalado.
4. **Maven** para la gestión de dependencias.


