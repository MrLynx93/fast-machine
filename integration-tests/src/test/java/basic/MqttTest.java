package basic;

import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.ServerConfiguration;
import com.agh.fastmachine.server.internal.transport.mqtt.MqttConfiguration;
import util.model.ExampleMqttInstanceProxy;
import util.model.ExampleObjectInstanceProxy;

public class MqttTest {
    private static String BROKER_ADDRESS = "tcp://localhost:1883";

    public static void main(String[] args) {
        Server server = new Server();
        MqttConfiguration transportConfiguration = new MqttConfiguration();
        transportConfiguration.setBrokerAddress(BROKER_ADDRESS);
        transportConfiguration.setServerId("123");
        transportConfiguration.setQos(1);

        ServerConfiguration configuration = new ServerConfiguration();
        configuration.addObjectSupport(ExampleMqttInstanceProxy.class);
        configuration.setTransport(ServerConfiguration.TRASPORT_MQTT);

        server.setConfiguration(configuration);
        server.start(transportConfiguration);
    }

}
