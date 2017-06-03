#!/usr/bin/env bash

#mvn clean install
pushd integration-tests

if [ "$2" == "mqtt" ]
then
    mvn exec:java -Dexec.mainClass="com.test.lifetime.StartMqttServer" -Dexec.args="$1" > "logs/server_lifetime_mqtt_$1.log" 2>1
fi

if [ "$2" == "coap" ]
then
    mvn exec:java -Dexec.mainClass="com.test.lifetime.StartCoapServer" -Dexec.args="$1" > "logs/server_lifetime_coap_$1.log" 2>1
fi

popd