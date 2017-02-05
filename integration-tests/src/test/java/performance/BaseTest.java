package performance;

import com.agh.fastmachine.core.api.model.resourcevalue.StringResourceValue;
import com.agh.fastmachine.server.api.ClientProxy;
import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.listener.RegistrationListener;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public abstract class BaseTest {
    private static final char[] CHARSET = "abcdefghijklmnopqrstuvwxyz1234567890".toCharArray();
    private static final int SERVERS_NUMBER = 5;
    public static final int CLIENTS_NUMBER = 5;
    public static final int TIMES = 1000;
    public static final int PAYLOAD_LENGTH = 1000;
    public static final String BROKER_ADDRESS = "ip addr..."; // TODO
    public static final String PAYLOAD = String.format("%0" + PAYLOAD_LENGTH + "d", 0);

    private CountDownLatch count = new CountDownLatch(CLIENTS_NUMBER);


    public abstract Server configureServer(int number);

    public void doTest() throws InterruptedException {
        List<Server> servers = new ArrayList<>();
        for (int i = 0; i < SERVERS_NUMBER; i++) {
            Server server = configureServer(i);
            server.setRegistrationListener(new RegistrationListener() {
                @Override
                public void onRegister(ClientProxy client) {
                    ObjectBaseProxy<TestInstanceProxy> testObject = client.getObjectTree().getObjectForType(TestInstanceProxy.class);
//                    TestInstanceProxy constantInstance = testObject.getInstance(0); TODO

                    for (int j = 0; j < TIMES; j++) {
                        // 1. Create
                        TestInstanceProxy newInstance = newInstance(client, server);
                        client.create(newInstance);

                        // 2. Write
                        newInstance.payload.setValue(newPayload());
                        newInstance.write();

                        // 3. Read
                        newInstance.read();

                        // 4. Delete
                        newInstance.delete();
                    }
                }

                @Override
                public void onDeregister(ClientProxy client) {
                    count.countDown();
                }
            });
            servers.add(server);
            server.start();
        }

        count.await();
        servers.forEach(Server::stop);
        System.out.println("Test ended");
    }


    private static TestInstanceProxy newInstance(ClientProxy client, Server server) {
        TestInstanceProxy instance = new TestInstanceProxy();
        instance.clientId = new StringResourceValue(client.getClientEndpointName());
//        instance.serverId = new StringResourceValue(server.getName()); // TODO
        instance.payload = new StringResourceValue(newPayload());
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
