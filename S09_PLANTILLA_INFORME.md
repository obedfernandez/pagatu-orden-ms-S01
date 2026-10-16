# INFORME TÉCNICO – S09
## Consistencia distribuida en procesos de negocio

**Proyecto:** Pagatu + Proyecto Sello del equipo
**Estudiante:** [Completar]
**Equipo:** [Completar]
**Curso:** Sistemas Distribuidos
**Rama / enlace de GitHub:** [Completar con tu nueva rama S09 publicada]

### 1. Idempotencia en Pagatu (sección 3.2 a 3.4)
- **Antes:** [Pegar captura real, a pantalla completa, del UNIQUE violado en S08]
- **Después:** [Pegar captura de Kafka UI y log `status=ignored` en S09]
- **Explicación:** `findByOrdenId` evita duplicar operaciones secuenciales y
  `UNIQUE(orden_id)` protege contra la carrera concurrente. El INSERT se aísla
  transaccionalmente para tratar una colisión después del rollback.

### 2. Pago fallido y compensación (3.5 a 3.7)
- [Captura pago FALLIDO y emisión pago.fallido]
- [Captura consumo pago.fallido / estado de orden CANCELADA]
- **Explicación:** Kafka comunica hechos, y Orden implementa su compensación
  como una transición local, sin acceder a la BD del otro servicio.

### 3. Compensación idempotente (3.8)
- [Captura de reenvío real del evento pago.fallido]
- [Captura de status=ignored y estado sin cambio]

### 4. Actividad autónoma del Proyecto Sello
- **Proceso propio:** [Nombre del proceso propio que cruza dos servicios]
- **Evento inicial:** [tipoEvento / topic]
- **Guardia y UNIQUE propia:** [tabla / índice / consulta]
- **Evento de compensación:** [tipoEvento / topic]
- **Diagrama de estados:** [Diagrama de tu entidad, no el de Pagatu]
- **Evidencia caso 1 duplicado:** [capturas]
- **Evidencia caso 2 fallo-compensación:** [capturas]
- **Evidencia caso 3 compensación duplicada:** [capturas]

### Bono Parte E
- Stock inicial: [valor y captura]
- Stock tras reserva: [valor y captura]
- Stock después de `CANCELADA`: [valor y captura]

### Error o hallazgo técnico real
[Relatar un error ocurrido durante tu ejecución, diagnóstico y solución.
No inventar errores.]

### Reflexión técnica (5 a 8 líneas, redactar tras las pruebas)
[Explicar por qué es difícil garantizar exactamente una vez; por qué se
prefiere al menos una vez más idempotencia; relacionar con Knight Capital.]

### Conclusiones
[Completar solo a partir de lo comprobado en tu máquina.]

---
### Anexo: feedback de la sesión (debe ser la última página del PDF)
1. ¿Cuál es el aprendizaje más importante que te llevas de la clase de hoy?
   [Respuesta]
2. ¿Qué punto de la clase te resultó más confuso o te dejó con dudas?
   [Respuesta]
3. ¿Tienes alguna pregunta que te gustaría que sea respondida la siguiente clase?
   [Respuesta]
4. Sobre tu nivel de comprensión de la clase de hoy:
   [¡Entendido! / Más o menos. / Necesito ayuda.]
5. ¿Cómo puedo ayudarte a comprender mejor el tema?
   [Respuesta]
6. ¿Cómo te autoevaluarías en participación y esfuerzo?
   [Muy Comprometido/a / Comprometido/a / Poco Comprometido/a]
7. Mi satisfacción con la clase fue... [1–10]
