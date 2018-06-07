#!/bin/sh
while [ 1 -eq 1 ]; do
	date
	cat ecg_discord_test.msgs | pv -q -L 1000| kafkacat -b localhost:9092 -P -t HealthSensorInputTopic
done
