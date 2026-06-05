# Piedrazul — Monorepo

Sistema de agendamiento de citas médicas.
Arquitectura de microservicios con Spring Boot + React.

## Servicios

| Servicio | Puerto | Descripción |
|---|---|---|
| api-gateway | 8090 | Enrutamiento central |
| identity-service | 8081 | Autenticación, usuarios, roles |
| medical-staff-service | 8082 | Médicos, especialidades, horarios |
| patient-service | 8083 | Pacientes |
| appointment-service | 8084 | Citas, disponibilidad, parámetros |
| piedrazul-desktop | — | Cliente JavaFX (iteración 1) |

## Levantar el entorno local

### Requisitos
- Docker Desktop instalado
- Java 21
- Node 20+

### Todos los servicios
```bash
docker-compose up -d
```

### Solo la base de datos (desarrollo local)
```bash
docker-compose up postgres -d
```
=======
#  Piedrazul — Red de Servicios Médicos

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-blue?style=for-the-badge&logo=java&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)

**Piedrazul** es una solución integral de software diseñada para la gestión eficiente de citas médicas. El sistema permite la administración centralizada de horarios, médicos y pacientes, optimizando el flujo de trabajo en centros de salud mediante una arquitectura robusta y escalable.

---

##  Arquitectura del Sistema

El proyecto implementa una **Arquitectura por Capas (N-Tier Architecture)** para garantizar la separación de responsabilidades y facilitar el mantenimiento:

* **Presentación (JavaFX):** Interfaz moderna y responsiva basada en el patrón **MVC** (Modelo-Vista-Controlador).
* **Aplicación (Fachada):** Uso del patrón **Facade** para centralizar la lógica de negocio y simplificar la interacción con los controladores.
* **Dominio:** Contiene las entidades de negocio (`User`, `Doctor`, `Patient`, `Appointment`) y las reglas lógicas centrales.
* **Infraestructura (Persistencia):** Gestión de datos mediante el patrón **DAO** (Data Access Object) y **Abstract Factory**, permitiendo el intercambio transparente entre diferentes motores de base de datos.

---

##  Características Principales

* **Autenticación Multirrol:** Sistema de inicio de sesión seguro con carga dinámica de perfiles (Médico, Agendador, Paciente).
* **Gestión Integral de Citas:** Registro, visualización y reprogramación de citas médicas con validación de disponibilidad.
* **Panel Administrativo:** Interfaz profesional con navegación lateral (Sidebar) y estados de carga asíncronos mediante `JavaFX Tasks`.
* **Principios de Diseño:** Código limpio guiado por principios **SOLID** y patrones de diseño estructurales.
* **Conectividad Cloud:** Persistencia de datos en la nube mediante PostgreSQL alojado en Railway.

---

##  Stack Tecnológico

* **Lenguaje:** Java 17+
* **Interfaz Gráfica:** JavaFX (CSS Personalizado)
* **Base de Datos:** PostgreSQL
* **Organización:** Notion y GitHub (Control de versiones)

---

##  Instalación y Ejecución

### Requisitos
* JDK 17 o superior.
* JavaFX SDK configurado.
* Conexión a internet para la base de datos en la nube.

### Pasos
1.  Clonar el repositorio:
    ```bash
    git clone [https://github.com/tu-usuario/piedrazul.git](https://github.com/tu-usuario/piedrazul.git)
    ```
2.  Configurar las credenciales en la clase `PostgreSQLConnection`.
3.  Ejecutar el proyecto desde la clase `App.java`.

---

##  Equipo de Desarrollo

Este proyecto fue desarrollado por estudiantes de **Ingeniería de Sistemas de la Universidad del Cauca**:

* **Valentina Añasco**
* **Camila Dorado**
* **Felipe Gutierrez**
* **Santiago Solarte**
* * **Ginner Ortega**

---

> !NOTA: Este software fue desarrollado como parte del curso de Ingeniería de Software II (Sexto Semestre).

