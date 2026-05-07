# Piedrazul — Monorepo

Sistema de agendamiento de citas médicas.
Arquitectura de microservicios con Spring Boot + React.

## Servicios

| Servicio | Puerto | Descripción |
|---|---|---|
| api-gateway | 8080 | Enrutamiento central |
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

### Base de datos
```bash
docker-compose up postgres -d
```

### Todos los servicios
```bash
docker-compose up -d
```

### Solo la base de datos (desarrollo local)
```bash
docker-compose up postgres -d
```
