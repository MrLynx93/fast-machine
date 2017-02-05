package performance;

import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.ServerConfiguration;
import com.agh.fastmachine.server.internal.transport.mqtt.MqttConfiguration;
import util.model.PingInstanceProxy;

public class MqttTest extends BaseTest {

    public static void main(String[] args) throws InterruptedException {
        CoapTest coapTest = new CoapTest();
        coapTest.doTest();
    }

    @Override
    public Server configureServer(int number) {
        Server server = new Server();
        server.setName("server_" + number);

        ServerConfiguration configuration = new ServerConfiguration();
        configuration.addObjectSupport(TestInstanceProxy.class);
        configuration.addObjectSupport(PingInstanceProxy.class);
        server.setConfiguration(configuration);

        MqttConfiguration transportConfiguration = new MqttConfiguration();
        transportConfiguration.setBrokerAddress(BROKER_ADDRESS);
        transportConfiguration.setQos(1);
        configuration.setTransport(ServerConfiguration.TRASPORT_MQTT);
        return server;
    }
}
