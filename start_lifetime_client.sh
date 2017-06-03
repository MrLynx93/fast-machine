#!/usr/bin/env bash

if [ "$2" == "coap" ]
then
#    mvn clean install
    pushd integration-tests
    for ((i=1; i<=$1; i++))
    do
        mvn exec:java -Dexec.mainClass="com.test.client." -Dexec.args="$i $3" > "logs/client_$i.log" 2>1 &
    done
    popd
fi