package basic;

import com.agh.fastmachine.core.api.model.resourcevalue.DoubleResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.IntegerResourceValue;
import com.agh.fastmachine.server.api.ClientProxy;
import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.ServerConfiguration;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.internal.transport.mqtt.MqttConfiguration;
import util.model.ExampleMqttInstanceProxy;

public class MqttTest {
    private static String BROKER_ADDRESS = "tcp://localhost:1883";

    public static void main(String[] args) throws InterruptedException {
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

        Thread.sleep(10000);
        ClientProxy client = server.getClientForEndpointName("lynx_ep");
        ObjectBaseProxy<ExampleMqttInstanceProxy> obj = client.getObjectTree().getObjectForType(ExampleMqttInstanceProxy.class);

        ExampleMqttInstanceProxy instance = obj.getInstance(1);
        instance.batteryLevel.setValue(new IntegerResourceValue(1));
        instance.doubleExampleResource.setValue(new DoubleResourceValue(0.01));
        instance.stringExampleResource.setValue(null);
        instance.write();

        Thread.sleep(1000);
        instance.batteryLevel.setValue(new IntegerResourceValue(2));
        instance.batteryLevel.write();

        Thread.sleep(100000);
    }

}
