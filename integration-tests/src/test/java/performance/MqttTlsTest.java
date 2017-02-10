package performance;

import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.ServerConfiguration;
import com.agh.fastmachine.server.bootstrap.BootstrapServer;
import com.agh.fastmachine.server.internal.transport.mqtt.MqttConfiguration;
import performance.model.TestInstanceProxy;
import util.model.PingInstanceProxy;

import java.util.List;

public class MqttTlsTest extends AbstractMqttTest {
    final String LOCAL_BROKER_ADDRESS = "localhost:1883";
    final String PUBLIC_BROKER_ADDRESS = "TODO"; // TODO

    public static void main(String[] args) throws InterruptedException {
        new MqttTlsTest().test();
    }

    @Override
    Server configureServer(int number) {
        ServerConfiguration configuration = new ServerConfiguration();
        configuration.setTransport(ServerConfiguration.TRASPORT_MQTT);
        configuration.setName("server_" + number);
        configuration.addObjectSupport(TestInstanceProxy.class);
        configuration.addObjectSupport(PingInstanceProxy.class);

        MqttConfiguration transportConfiguration = new MqttConfiguration();
        transportConfiguration.setBrokerAddress(LOCAL_BROKER_ADDRESS);
        transportConfiguration.setQos(1);
        transportConfiguration.setDtls(true);
        transportConfiguration.setKeyStoreLocation("ca.jks");
        transportConfiguration.setKeyStorePassword("123456");
        return new Server(configuration, transportConfiguration);
    }

    @Override
    BootstrapServer configureBootstrapServer(List<Server> servers) {
        MqttConfiguration mqttConfiguration = new MqttConfiguration();
        mqttConfiguration.setQos(1);
        mqttConfiguration.setBrokerAddress(LOCAL_BROKER_ADDRESS);
        mqttConfiguration.setServerName("bootstrap-server");
        mqttConfiguration.setDtls(true);
        mqttConfiguration.setKeyStoreLocation("ca.jks");
        mqttConfiguration.setKeyStorePassword("123456");

        BootstrapServer bootstrapServer = new BootstrapServer(mqttConfiguration);
        bootstrapServer.setSequenceForPattern(".*", configureBootstrapSequence(servers));
        return bootstrapServer;
    }
}
