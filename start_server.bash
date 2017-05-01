#!/usr/bin/env bash


#mvn clean install
pushd integration-tests

if [ "$2" == "mqtt" ]
then
    mvn exec:java -Dexec.mainClass="com.test.server.MqttStartTestServer" -Dexec.args="$1 $3" > logs/server_mqtt.log 2>1
fi

if [ "$2" == "coap" ]
then
    mvn exec:java -Dexec.mainClass="com.test.server.CoapStartTestServer" -Dexec.args="$1 $3" > logs/server_coap.log 2>1
fi

popd