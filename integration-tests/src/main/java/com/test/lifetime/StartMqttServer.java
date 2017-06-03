package com.test.lifetime;

import com.agh.fastmachine.server.api.ClientProxy;
import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.ServerConfiguration;
import com.agh.fastmachine.server.api.listener.RegistrationListener;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.internal.transport.TransportConfiguration;
import com.agh.fastmachine.server.internal.transport.mqtt.MqttConfiguration;
import com.agh.fastmachine.server.internal.transport.stats.TimeoutException;
import com.test.model.test.TestInstanceProxy;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StartMqttServer {
    private static final boolean TLS = false;
    private static final String BROKER_ADDRESS = "34.250.196.139:1883"; //TODO
    private static final String SERVER_NAME = "main_server_1";
    private static final long SLEEPTIME = 1000;

    private static ExecutorService executor = Executors.newFixedThreadPool(2);

    /**
     * This function is to experiment
     *
     */
    private static void handleClient(ClientProxyImpl client) {
        for (int i = 0; i < 100; i++) {
            ObjectBaseProxy<TestInstanceProxy> exampleObj = client.getObjectTree().getObjectForType(TestInstanceProxy.class);
            TestInstanceProxy instance = exampleObj.getInstance(0);

            try {
                instance.payload.read();
                String formattedDate = DateFormat.getDateTimeInstance().format(new Date());
                System.out.println("Last read sent: " + formattedDate);

            } catch (TimeoutException e) {
                System.out.println("Cos sie popsulo");
                e.printStackTrace();
            }

            sleepForMills();

            System.out.println(i);
        }
        System.out.println("Finished test");

    }

    public static void main(String[] args) {
        StartBootstrapServer.main(args);
        Server server = new Server(configureServer(), configureTransport());
        server.setRegistrationListener(registrationListener);
        server.start();
        System.out.println("You should now run all clients.");
    }

    private static ServerConfiguration configureServer() {
        ServerConfiguration configuration = new ServerConfiguration();
        configuration.setName(SERVER_NAME);
        configuration.addObjectSupport(TestInstanceProxy.class);
        configuration.setTransport(ServerConfiguration.TRASPORT_MQTT);
        return configuration;
    }

    private static TransportConfiguration configureTransport() {
        MqttConfiguration mqttConfiguration = new MqttConfiguration();
        mqttConfiguration.setBrokerAddress(BROKER_ADDRESS);
        mqttConfiguration.setServerName(SERVER_NAME);
        mqttConfiguration.setQos(1);
        if (TLS) {
            mqttConfiguration.setDtls(true);
            mqttConfiguration.setKeyStoreLocation("ca.jks");
            mqttConfiguration.setKeyStorePassword("123456");
        }
        return mqttConfiguration;
    }

    private static RegistrationListener registrationListener = new RegistrationListener() {
        @Override
        public void onRegister(ClientProxy client) {
            System.out.println("Registered client " + client.getClientEndpointName());
            executor.submit(() -> handleClient((ClientProxyImpl) client));
        }

        @Override
        public void onDeregister(ClientProxy client) {
            System.out.println("Deregistered client " + client.getClientEndpointName());
            // TODO deregister
        }

        @Override
        public void onUpdate(ClientProxy client) {
            String formattedDate = DateFormat.getDateTimeInstance().format(new Date());
            System.out.println("Last update received " + formattedDate);
        }
    };

    private static void sleepForMills() {
        try {
            Thread.sleep(SLEEPTIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
