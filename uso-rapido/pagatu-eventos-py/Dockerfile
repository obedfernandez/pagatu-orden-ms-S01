FROM python:3.13-alpine
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY producer_ordenes.py consumer_ordenes.py ./
CMD ["python", "-c", "print('Contenedor listo. Ejecuta producer_ordenes.py o consumer_ordenes.py manualmente.')"]
