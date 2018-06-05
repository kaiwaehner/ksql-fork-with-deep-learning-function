#!/bin/sh

curl -X "POST" "http://localhost:8083/connectors/" \
   -H "Content-Type: application/json" \
   -d '{
    "name": "es_sink_raw_events",
    "config": {
      "topics": "SENSOR_RAW",
      "key.converter": "org.apache.kafka.connect.storage.StringConverter",
      "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
      "key.ignore": "true",
      "schema.ignore": "true",
      "type.name": "type.name=kafkaconnect",
      "topic.index.map": "SENSOR_RAW:healthsensorinput_raw",
      "connection.url": "http://localhost:9200",
      "transforms": "ExtractTimestamp",
      "transforms.ExtractTimestamp.type": "org.apache.kafka.connect.transforms.InsertField$Value",
      "transforms.ExtractTimestamp.timestamp.field" : "EXTRACT_TS"
    }
  }'

curl -X "POST" "http://localhost:8083/connectors/" \
         -H "Content-Type: application/json" \
         -d '{
      "name": "es_sink_anomaly",
      "config": {
        "topics": "ANOMALYDETECTION",
        "key.converter": "org.apache.kafka.connect.storage.StringConverter",
        "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
        "key.ignore": "true",
        "schema.ignore": "true",
        "type.name": "type.name=kafkaconnect",
        "topic.index.map": "ANOMALYDETECTION:healthsensorinput_scored",
        "connection.url": "http://localhost:9200",
        "transforms": "ExtractTimestamp",
        "transforms.ExtractTimestamp.type": "org.apache.kafka.connect.transforms.InsertField$Value",
        "transforms.ExtractTimestamp.timestamp.field" : "EXTRACT_TS"
      }
    }'

curl -X "POST" "http://localhost:8083/connectors/" \
         -H "Content-Type: application/json" \
         -d '{
      "name": "es_sink_anomaly_alerts",
      "config": {
        "topics": "ANOMALYDETECTIONBREACH",
        "key.converter": "org.apache.kafka.connect.storage.StringConverter",
        "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
        "key.ignore": "true",
        "schema.ignore": "true",
        "type.name": "type.name=kafkaconnect",
        "topic.index.map": "ANOMALYDETECTIONBREACH:healthsensorinput_alerts",
        "connection.url": "http://localhost:9200",
        "transforms": "ExtractTimestamp",
        "transforms.ExtractTimestamp.type": "org.apache.kafka.connect.transforms.InsertField$Value",
        "transforms.ExtractTimestamp.timestamp.field" : "EXTRACT_TS"
      }
    }'
