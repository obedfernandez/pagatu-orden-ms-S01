import json
from kafka import KafkaConsumer


def emit(payload):
    print(json.dumps(payload, ensure_ascii=False, separators=(",", ":")), flush=True)


consumer = KafkaConsumer(
    "orden-eventos",
    bootstrap_servers="kafka:9092",
    group_id="pagatu-eventos-py-group",
    auto_offset_reset="earliest",
    enable_auto_commit=True,
)

emit({"component": "consumer", "status": "ready", "topic": "orden-eventos"})
try:
    for message in consumer:
        raw = message.value.decode("utf-8", errors="replace")
        try:
            evento = json.loads(raw)
            if not isinstance(evento, dict):
                raise ValueError("el payload JSON debe ser un objeto")

            emit(
                {
                    "component": "consumer",
                    "topic": message.topic,
                    "partition": message.partition,
                    "offset": message.offset,
                    "eventType": evento.get("tipoEvento"),
                    "ordenId": evento.get("ordenId"),
                    "idCliente": evento.get("idCliente"),
                    "metodoPago": evento.get("metodoPago"),
                    "status": "consumed",
                }
            )
        except Exception as exc:
            emit(
                {
                    "component": "consumer",
                    "topic": message.topic,
                    "partition": message.partition,
                    "offset": message.offset,
                    "status": "invalid",
                    "rawPayload": raw,
                    "decodeError": str(exc),
                }
            )
except KeyboardInterrupt:
    pass
finally:
    consumer.close()
