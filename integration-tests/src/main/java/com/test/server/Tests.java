package com.test.server;

import com.agh.fastmachine.core.api.model.resourcevalue.StringResourceValue;
import com.agh.fastmachine.server.api.ClientProxy;
import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.test.performance.model.TestInstanceProxy;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Tests {
    private static final ExecutorService executor = Executors.newFixedThreadPool(5);
    public static final int ITERATIONS = 10;

    public static void testMqtt(Server server) {
        testSingle(server);
    }

    public static void testCoap(Server server) {
        System.out.println("Started coap test");
        testSingle(server);
    }

    private static void testSingle(Server server) {
        server.getClients().values().forEach(client -> executor.submit(() -> {
            int clientIdx = getClientIdx(client);
            ObjectBaseProxy<TestInstanceProxy> testObject = client.getObjectTree().getObjectForType(TestInstanceProxy.class);
            TestInstanceProxy constantInstance = testObject.getInstance(0);
            TestInstanceProxy newInstance = Generator.newInstance(client, server);
            client.create(newInstance, clientIdx);

            for (int i = 0; i < ITERATIONS; i++) {
                newInstance.payload.setValue(new StringResourceValue(Generator.newPayload()));
                newInstance.payload.write();

                constantInstance.clientId.read();

                sleep();
            }

            System.out.println("Finished operations for client " + client.getClientEndpointName());
        }));
    }

    private static void sleep() {
        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void testMqttBroadcast(Server server) {
    }

    public static void testCoapBroadcast(Server server) {
    }

    private static int getClientIdx(ClientProxy client) {
        int clientIdx = Integer.parseInt(client.getClientEndpointName().split("_")[1]);
        return clientIdx > 10000 ? clientIdx - 29000 : clientIdx;
    }

    private static class Generator {
        private static final char[] CHARSET = "abcdefghijklmnopqrstuvwxyz1234567890".toCharArray();
        private static final int PAYLOAD_LENGTH = 1000;
        /**
         * New instance has ID of server. Constant instance has ID=0
         */
        private static TestInstanceProxy newInstance(ClientProxy client, Server server) {
            TestInstanceProxy instance = new TestInstanceProxy();
            instance.clientId.setValue(new StringResourceValue(client.getClientEndpointName()));
            instance.serverId.setValue(new StringResourceValue(server.getName()));
            instance.payload.setValue(new StringResourceValue(newPayload()));
            return instance;
        }

        private static String newPayload() {
            Random random = new SecureRandom();
            char[] result = new char[PAYLOAD_LENGTH];
            for (int i = 0; i < result.length; i++) {
                // picks a random index out of character set > random character
                int randomCharIndex = random.nextInt(CHARSET.length);
                result[i] = CHARSET[randomCharIndex];
            }
            return new String(result);
        }
    }
}
