package basic;

import com.agh.fastmachine.server.api.ClientProxy;
import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.ServerConfiguration;
import com.agh.fastmachine.server.api.listener.RegistrationListener;
import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.internal.transport.TransportConfiguration;
import com.agh.fastmachine.server.internal.transport.mqtt.MqttConfiguration;
import util.model.AndroidUtilsInstanceProxy;
import util.model.ExampleMqttInstanceProxy;
import util.model.PingInstanceProxy;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StartMqttServer {
    private static final boolean TLS = false;
    private static final String BROKER_ADDRESS = "TODO"; //TODO
    private static final String SERVER_NAME = "main_server_1";

    private static ExecutorService executor = Executors.newFixedThreadPool(2);

    /**
     * This function is to experiment
     *
     */
    private static void handleClient(ClientProxyImpl client) {


    }

    public static void main(String[] args) {
        Server server = new Server(configureServer(), configureTransport());
        server.setRegistrationListener(registrationListener);
        server.start();
    }

    private static ServerConfiguration configureServer() {
        ServerConfiguration configuration = new ServerConfiguration();
        configuration.addObjectSupport(AndroidUtilsInstanceProxy.class);
        configuration.addObjectSupport(ExampleMqttInstanceProxy.class);
        configuration.addObjectSupport(PingInstanceProxy.class);
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
    };
}
