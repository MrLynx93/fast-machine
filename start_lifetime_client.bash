#!/usr/bin/env bash


if [ "$2" == "mqtt" ]
then
    pushd ././../lynx-lwm2m/
    make clean all
    make runtest ARGS="costam 0" 2>&1 | tee "logs/client_mqtt_lifetime_$1.log"
    popd
fi

if [ "$2" == "coap" ]
then
#    mvn clean install
    pushd integration-tests
    mvn exec:java -Dexec.mainClass="com.test.lifetime.StartCoapClient" -Dexec.args="$1" 2>&1 | tee "logs/client_coap_lifetime_$1.log"
    popd
fi


