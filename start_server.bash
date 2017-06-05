#!/usr/bin/env bash


#mvn clean install
pushd integration-tests

if [ "$2" == "mqtt" ]
then
    mvn exec:java -Dexec.mainClass="com.test.server.MqttStartTestServer" -Dexec.args="$1 $3" 2>&1 | tee logs/server_mqtt.log
fi

if [ "$2" == "coap" ]
then
    mvn exec:java -Dexec.mainClass="com.test.server.CoapStartTestServer" -Dexec.args="$1 $3" 2>&1 | tee logs/server_coap.log
fi

popd