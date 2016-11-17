package basic;

import com.agh.fastmachine.core.api.model.resourcevalue.*;
import com.agh.fastmachine.server.api.ClientProxy;
import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.ServerConfiguration;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.internal.transport.mqtt.MqttConfiguration;
import util.model.ExampleMqttInstanceProxy;

import java.util.Arrays;

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

        Thread.sleep(5000);
        ClientProxy client = server.getClientForEndpointName("lynx_ep");
        ObjectBaseProxy<ExampleMqttInstanceProxy> obj = client.getObjectTree().getObjectForType(ExampleMqttInstanceProxy.class);

        ExampleMqttInstanceProxy instance = new ExampleMqttInstanceProxy();
        instance.batteryLevel.setValue(new IntegerResourceValue(10));
        instance.doubleExampleResource.setValue(new DoubleResourceValue(0.1));
        instance.stringExampleResource.setValue(new StringResourceValue("ABC"));
        instance.lightOn.setValue(new BooleanResourceValue(false));
        instance.opaqueExampleResource.setValue(new OpaqueResourceValue("ABC".getBytes()));
        instance.firmwireUpdateResource.setValue(null);
        instance.linkExampleResource.setValue(new LinkResourceValue(new Link(1, 1)));
        instance.optionalIntegerResource.setValue(new IntegerResourceValue(12));
        instance.multipleStringExample.setValues(Arrays.asList(new StringResourceValue("A"), new StringResourceValue("B")));
        instance.multipleOptionalStringExample.setValues(null);
        client.create(instance, 20);

        Thread.sleep(2000);
        instance.batteryLevel.setValue(new IntegerResourceValue(99));
        instance.batteryLevel.write();

        Thread.sleep(2000);
        instance.read();

        System.out.println("END");
    }

}
