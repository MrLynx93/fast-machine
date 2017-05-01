package com.test.server;

import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.bootstrap.BootstrapServer;
import com.test.Tests;
import com.test.Utils;

import java.util.concurrent.CountDownLatch;

public class MqttStartTestServer {
    private static CountDownLatch registerCount;
    private static CountDownLatch deregisterCount;
    private static boolean tls;
    private static int clientsNumber;

    public static void main(String[] args) throws InterruptedException {
        if (args.length < 1) {
            return;
        }
        clientsNumber = Integer.parseInt(args[0]);
        tls = args.length == 2 && args[1].equals("tls");
        registerCount = new CountDownLatch(clientsNumber);
        deregisterCount = new CountDownLatch(clientsNumber);

        // Start bootstrap server
        BootstrapServer bootstrapServer = Utils.MQTT.configureBootstrapServer(tls);
        bootstrapServer.start();

        // Start server
        Server server = Utils.MQTT.configureServer(tls);
        server.setRegistrationListener(Utils.counterListener(registerCount, deregisterCount));
        server.start();

        // Wait for register all clients, then do all test
        System.out.println("You should now run all clients.");
        registerCount.await();
        if (clientsNumber > 1) {
            Tests.testMqttBroadcast(server);
        } else {
            Tests.testMqtt(server);
        }

        // Wait for deregister, then exit
        deregisterCount.await();
        server.getStats().logStats();
        server.stop();
        System.exit(0);
    }
}
