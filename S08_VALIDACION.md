# S08 - Validación realizada

Se verificó antes de empaquetar:

- Sintaxis de los 8 `pom.xml` mediante parser XML.
- Sintaxis de los YAML relevantes de configuración, Kafka, pago y cliente Python.
- Sintaxis de los scripts Python.
- Parseo sintáctico de 66 archivos Java de `pagatu-pago-ms`, `pagatu-orden-ms` y `pagatu-cliente-ms` con JDK 21.
- `git diff --check` sin errores de espacios/conflictos.
- Presencia de Kafka/Kafka UI DEV y PROD, microservicio de pagos, migración, productores/consumidores, contratos, prueba punta a punta y evento autónomo.

## Validación que debes ejecutar localmente

Este entorno no pudo descargar Maven desde Maven Central y no dispone de Docker, por lo que la compilación completa con dependencias y la prueba de contenedores deben ejecutarse en tu PC:

```powershell
cd services/pagatu-pago-ms
.\mvnw.cmd clean package

cd ..\pagatu-orden-ms
.\mvnw.cmd clean package

cd ..\pagatu-cliente-ms
.\mvnw.cmd clean package
```

Después sigue `S08_PASOS_EJECUCION.md` y ejecuta `S08_PRUEBAS.ps1`.
