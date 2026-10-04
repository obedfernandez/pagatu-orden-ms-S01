import json
import time
from datetime import datetime, timezone
from kafka import KafkaProducer


def emit(payload):
    print(json.dumps(payload, ensure_ascii=False, separators=(",", ":")), flush=True)


producer = KafkaProducer(
    bootstrap_servers="kafka:9092",
    key_serializer=lambda value: str(value).encode("utf-8"),
    value_serializer=lambda value: json.dumps(value).encode("utf-8"),
)

contador = 1
emit({"component": "producer", "status": "ready", "topic": "orden-eventos"})
try:
    while True:
        orden_id = 900000 + contador
        evento = {
            "tipoEvento": "orden.creada",
            "ordenId": orden_id,
            "idCliente": 1,
            "total": round(20.00 + contador, 2),
            "metodoPago": "YAPE_PLIN",
            "origen": "pagatu-eventos-py",
            "timestamp": int(datetime.now(timezone.utc).timestamp() * 1000),
        }
        metadata = producer.send("orden-eventos", key=orden_id, value=evento).get(timeout=10)
        emit(
            {
                "component": "producer",
                "topic": metadata.topic,
                "partition": metadata.partition,
                "offset": metadata.offset,
                "eventType": evento["tipoEvento"],
                "ordenId": evento["ordenId"],
                "idCliente": evento["idCliente"],
                "metodoPago": evento["metodoPago"],
                "status": "published",
            }
        )
        contador += 1
        time.sleep(2)
except KeyboardInterrupt:
    pass
finally:
    producer.flush()
    producer.close()
