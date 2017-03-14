#!/usr/bin/env bash
cd integration-tests
mvn install
mvn exec:java -Dexec.mainClass="com.test.performance.coap.server.CoapTestServer"