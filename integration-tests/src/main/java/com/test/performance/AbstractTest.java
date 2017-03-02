package com.test.performance;

import com.agh.fastmachine.core.api.model.resourcevalue.StringResourceValue;
import com.agh.fastmachine.server.api.ClientProxy;
import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.listener.RegistrationListener;
import com.agh.fastmachine.server.internal.client.ClientProxyStatus;
import com.test.performance.coap.client.AbstractCoapTestClient;
import com.test.performance.model.TestInstanceProxy;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractTest {
    private static final char[] CHARSET = "abcdefghijklmnopqrstuvwxyz1234567890".toCharArray();
    private static final int PAYLOAD_LENGTH = 1000;

    public static String SERVER_URL = "localhost";
    public static int TIMES = 100;
    public static int SERVERS_NUMBER = 1;
    public static int CLIENTS_NUMBER = 1;
    public static int LIFETIME = 3600;

    static {
        try {
            Properties properties = new Properties();
            ClassLoader loader = AbstractCoapTestClient.class.getClassLoader();
            InputStream inputStream = loader.getResourceAsStream("test.properties");
            properties.load(inputStream);
            SERVERS_NUMBER = Integer.parseInt(properties.getProperty("servers"));
            CLIENTS_NUMBER = Integer.parseInt(properties.getProperty("clients"));
            SERVER_URL = "coap://" + properties.getProperty("coap.serverUrl") + ":";
            TIMES = Integer.parseInt(properties.getProperty("times"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ExecutorService executor = Executors.newFixedThreadPool(SERVERS_NUMBER * CLIENTS_NUMBER);
    public CountDownLatch deregisteredCount = new CountDownLatch(SERVERS_NUMBER * CLIENTS_NUMBER);
    public CountDownLatch registeredCount = new CountDownLatch(SERVERS_NUMBER * CLIENTS_NUMBER);

    public void prepareTest(final Server server) {
        server.setRegistrationListener(new RegistrationListener() {
            @Override
            public void onRegister(ClientProxy client) {
                registeredCount.countDown();

                executor.submit(() -> {
                    int serverId = Integer.parseInt(server.getName().split("_")[1]);
                    TestInstanceProxy constantInstance = client.getObjectTree().getObjectForType(TestInstanceProxy.class).getInstance(0);

                    TestInstanceProxy newInstance = newInstance(client, server);
                    if (client.getStatus() == ClientProxyStatus.REGISTERED) {
                        client.create(newInstance, serverId);
                    }

//                    for (int i = 0; i < TIMES; i++) {
                    while (client.getStatus() == ClientProxyStatus.REGISTERED) {
                        // 1. Create

                        // 2. Write
                        if (client.getStatus() == ClientProxyStatus.REGISTERED) {
                            newInstance.payload.setValue(new StringResourceValue(newPayload()));
                            newInstance.payload.write();
                        }
                        // 3. Read
//                        if (client.getStatus() == ClientProxyStatus.REGISTERED) {
//                            newInstance.read();
//                        }
                        // 4. Read (to count operations)
                        if (client.getStatus() == ClientProxyStatus.REGISTERED) {
                            constantInstance.clientId.read();
                        }
                        // 5. Delete
//                        if (client.getStatus() == ClientProxyStatus.REGISTERED) {
//                            newInstance.delete();
//                        }

//                        try {
//                            Thread.sleep(100 + new Random().nextInt(200));
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                    }
                });
            }

            @Override
            public void onDeregister(ClientProxy client) {
                deregisteredCount.countDown();
            }
        });
    }

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
