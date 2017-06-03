#!/usr/bin/env bash


pushd integration-tests
mvn exec:java -Dexec.mainClass="com.test.lifetime.StartCoapClient" -Dexec.args="$1" > "logs/client_lifetime_$1.log" 2>1 &
popd