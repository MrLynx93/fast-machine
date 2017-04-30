#!/usr/bin/env bash


if [ "$2" == "mqtt" ]
then
    pushd ././../lynx-lwm2m/
    make clean all
    for ((i=1; i<=$1; i++))
    do
        make runtest ARGS="$i $3" > "logs/client_$i.log" 2>1 &
    done
    popd
fi

if [ "$2" == "coap" ]
then
#    mvn clean install
    pushd integration-tests
    for ((i=1; i<=$1; i++))
    do
        mvn exec:java -Dexec.mainClass="com.test.client.coap.CoapStartTestClient" -Dexec.args="$i $3" > "logs/client_$i.log" 2>1 &
    done
    popd
fi