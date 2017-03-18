#!/usr/bin/env bash
cd integration-tests
mvn clean install
mvn exec:java -Dexec.mainClass="com.test.performance.coap.client.CoapTestClient"