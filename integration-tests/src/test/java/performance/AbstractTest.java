package performance;

import com.agh.fastmachine.core.api.model.resourcevalue.StringResourceValue;
import com.agh.fastmachine.server.api.ClientProxy;
import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.listener.RegistrationListener;
import com.agh.fastmachine.server.internal.client.ClientProxyStatus;
import performance.model.TestInstanceProxy;
import util.TestUtil;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

abstract class AbstractTest {
    private static final char[] CHARSET = "abcdefghijklmnopqrstuvwxyz1234567890".toCharArray();
    private static final int PAYLOAD_LENGTH = 1000;
    static final int TIMES = 1000;
    static final int SERVERS_NUMBER = 1;
    static final int CLIENTS_NUMBER = 1;
    static final int LIFETIME = 3600;

    ExecutorService executor = Executors.newFixedThreadPool(SERVERS_NUMBER * CLIENTS_NUMBER);
    CountDownLatch deregisteredCount = new CountDownLatch(SERVERS_NUMBER * CLIENTS_NUMBER);
    CountDownLatch registeredCount = new CountDownLatch(SERVERS_NUMBER * CLIENTS_NUMBER);

    void prepareTest(final Server server) {
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
//                        if (client.getStatus() == ClientProxyStatus.REGISTERED) {
//                            newInstance.payload.setValue(new StringResourceValue(newPayload()));
//                            newInstance.payload.write();
//                        }
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
