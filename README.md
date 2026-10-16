> **S07b Keycloak:** consulta primero [S07B_LEEME_PRIMERO.md](S07B_LEEME_PRIMERO.md) para levantar el proyecto actualizado.

# pagatu

Sistema distribuido de comercio electrónico — proyecto del curso **Desarrollo de Aplicaciones Distribuidas** (DIST), UPeU 2026-2.

## Documentación

La documentación completa del curso (sílabo, guías de sesión paso a paso, arquitectura del sistema) vive en [`docs/`](docs/index.md) y se publica como sitio con MkDocs:

**[262dist.github.io/pagatu](https://262dist.github.io/pagatu/)**

## Estructura del repositorio

```text
pagatu/
├── docs/                    # Documentación del curso (MkDocs)
│   ├── sesiones/             # Guías paso a paso (S01, S02, ...)
│   └── silabo_dist_2026_2.md # Sílabo vigente
├── services/                 # Microservicios
│   ├── pagatu-catalogo-ms/   # Categorías y productos (S1)
│   ├── pagatu-orden-ms/      # Órdenes + eventos Kafka (S2/S8)
│   ├── pagatu-cliente-ms/    # Perfil de cliente + consumidor autónomo S8
│   ├── pagatu-auth-ms/       # Autenticación/JWT (S7)
│   └── pagatu-pago-ms/       # Pagos asíncronos con Kafka (S8)
├── infra/                    # Infraestructura que los microservicios necesitan para funcionar
│   ├── pagatu-config/        # Config Server (S2, listo)
│   ├── pagatu-eureka/        # Service Registry (S3, listo)
│   └── pagatu-gateway/       # API Gateway (S4/S7/S8)
├── kafka/                     # Kafka + Kafka UI (S8)
├── uso-rapido/                # Cliente Python Kafka (S8)
├── obs/                       # Prometheus + Loki + Promtail (S3, opcional)
└── clients/                  # Frontend Angular (pendiente, desde S11)
```

## Arquitectura

Diagramas C4 (contexto y contenedores) en [`docs/index.md`](docs/index.md#arquitectura-pagatu-v2026).

## Cómo ejecutar un microservicio en DEV

Cada microservicio en `services/` sigue el mismo patrón: PostgreSQL en Docker, la aplicación con Maven Wrapper desde el host.

```powershell
cd services/pagatu-catalogo-ms
docker compose -f compose-dev.yml up -d
.\mvnw.cmd spring-boot:run
```

El detalle completo (puertos, variables de entorno, producción local con Docker) está en la guía de sesión correspondiente dentro de `docs/sesiones/`.

## Tecnologías

- Java 21, Spring Boot 4.1.1
- PostgreSQL, Flyway (migraciones versionadas)
- MapStruct (mapeo DTO ↔ entidad)
- Docker / Docker Compose
- Spring Cloud Config (S2), Eureka (S3), Gateway (S4), JWT (S7), Spring for Apache Kafka (S8)
- Angular 21 (S11)


## S08 - Mensajería asíncrona

La implementación de S08 está documentada en `S08_COMPLETADO.md`, `S08_PASOS_EJECUCION.md`, `S08_EVIDENCIA_GUIA.md` y `docs/S08_CONTRATOS_EVENTOS.md`.

## Sesión S09 – Consistencia distribuida (sobre S08)

Esta versión conserva los avances personalizados de S08 y agrega idempotencia,
`pago.fallido`, Saga compensadora e implementación opcional de stock. Para
preparar Windows/Docker y reproducir las pruebas reales de S09, empieza por
[S09_EJECUCION_Y_EVIDENCIAS.md](S09_EJECUCION_Y_EVIDENCIAS.md), luego ejecuta
`S09_PRUEBAS.ps1`. Documentación del ciclo de vida y contratos:
[docs/S09_ESTADOS_Y_CONTRATOS.md](docs/S09_ESTADOS_Y_CONTRATOS.md).

**Importante:** la actividad autónoma del Proyecto Sello de la sección 4 de
la guía sigue requiriendo el código del proyecto propio y evidencias reales.
