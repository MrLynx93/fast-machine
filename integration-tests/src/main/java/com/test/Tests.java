package com.test;

import com.agh.fastmachine.core.api.model.resourcevalue.StringResourceValue;
import com.agh.fastmachine.server.api.ClientProxy;
import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.internal.transport.stats.TimeoutException;
import com.test.model.test.TestInstanceProxy;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Tests {
    private static final ExecutorService executor = Executors.newFixedThreadPool(5);
    private static final ExecutorService asyncExecutor = Executors.newFixedThreadPool(5);

    private static final boolean async = true;

    public static final int ITERATIONS = 100;

    public static void testMqtt(Server server) {
        try {
            testSingle(server);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void testCoap(Server server) {
        System.out.println("Started coap test");
        try {
            testSingle(server);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testSingle(Server server) throws Exception {

        server.getClients().values().forEach(client -> executor.submit(() -> {
            int clientIdx = getClientIdx(client);
            ObjectBaseProxy<TestInstanceProxy> testObject = client.getObjectTree().getObjectForType(TestInstanceProxy.class);
            TestInstanceProxy constantInstance = testObject.getInstance(0);
            TestInstanceProxy newInstance = Generator.newInstance(client, server);
            client.create(newInstance, clientIdx);

            for (int i = 0; i < ITERATIONS; i++) {
                if (async) {
                    asyncExecutor.submit(() -> {
                        long startTime = System.currentTimeMillis();
                        try {
                            constantInstance.clientId.read();
                            long endTime = System.currentTimeMillis();
                            System.out.println(String.format("(%d,%d)", startTime, endTime));
                        } catch (TimeoutException ex) {
                            System.out.println(String.format("(%d,inf)", startTime));
                        }

                    });
                } else {
                    newInstance.payload.setValue(new StringResourceValue(Generator.newPayload()));
                    newInstance.payload.write();

                    try {
                        constantInstance.clientId.read();
                    } catch (TimeoutException ignored) { }
                }
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

    private static void testBroadcast(Server server) {
        Server.InstanceCreator instanceCreator = () -> Generator.newInstance(server);
        server.createAll(instanceCreator, 1000);

        ClientProxy anyClient = server.getClients().values().iterator().next();
        ObjectBaseProxy<TestInstanceProxy> testObject = anyClient.getObjectTree().getObjectForType(TestInstanceProxy.class);
        TestInstanceProxy constantInstance = testObject.getInstance(0);
        TestInstanceProxy newInstance = testObject.getInstance(1000);

        for (int i = 0; i < ITERATIONS; i++) {
            newInstance.payload.setValue(new StringResourceValue(Generator.newPayload()));
            newInstance.payload.writeAll();
            constantInstance.clientId.readAll();
            sleep();
        }
    }

    public static void testMqttBroadcast(Server server) {
        testBroadcast(server);
    }

    public static void testCoapBroadcast(Server server) {
        testBroadcast(server);
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


        private static TestInstanceProxy newInstance(Server server) {
            TestInstanceProxy instance = new TestInstanceProxy();
            instance.clientId.setValue(new StringResourceValue("anyclient"));
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
