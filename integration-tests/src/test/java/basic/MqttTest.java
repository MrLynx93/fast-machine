package basic;

import com.agh.fastmachine.core.api.model.resourcevalue.*;
import com.agh.fastmachine.server.api.ClientProxy;
import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.ServerConfiguration;
import com.agh.fastmachine.server.api.listener.ObservationListener;
import com.agh.fastmachine.server.api.listener.RegistrationListener;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;
import com.agh.fastmachine.server.internal.client.ClientProxyStatus;
import com.agh.fastmachine.server.internal.transport.mqtt.MqttConfiguration;
import util.model.AndroidUtilsInstanceProxy;
import util.model.ExampleMqttInstanceProxy;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MqttTest {
    private static final String PUBLIC_BROKER_ADDRESS = "tcp://broker.hivemq.com:1883";
    private static final String AMAZON_BROKER_ADDRESS = "tcp://ec2-52-212-253-117.eu-west-1.compute.amazonaws.com:1883";
    private static final String PRIVATE_BROKER_ADDRESS = "tcp://localhost:1883";


    private static String BROKER_ADDRESS = AMAZON_BROKER_ADDRESS;

    public static void main(String[] args) throws InterruptedException {

        ExecutorService executor = Executors.newSingleThreadExecutor();

        Server server = new Server();
        MqttConfiguration transportConfiguration = new MqttConfiguration();
        transportConfiguration.setBrokerAddress(BROKER_ADDRESS);
        transportConfiguration.setServerId("123");
        transportConfiguration.setQos(1);

        ServerConfiguration configuration = new ServerConfiguration();
        configuration.addObjectSupport(ExampleMqttInstanceProxy.class);
        configuration.addObjectSupport(AndroidUtilsInstanceProxy.class);
        configuration.setTransport(ServerConfiguration.TRASPORT_MQTT);

        server.setConfiguration(configuration);

        server.setRegistrationListener(new RegistrationListener() {
            @Override
            public void onRegister(ClientProxy client) {
                executor.submit(() -> {
//                    ObjectBaseProxy<ExampleMqttInstanceProxy> obj = client.getObjectTree().getObjectForType(ExampleMqttInstanceProxy.class);
                    try {
//                        ExampleMqttInstanceProxy instance = new ExampleMqttInstanceProxy();
//                        instance.batteryLevel.setValue(new IntegerResourceValue(10));
//                        instance.doubleExampleResource.setValue(new DoubleResourceValue(0.1));
//                        instance.stringExampleResource.setValue(new StringResourceValue("ABC"));
//                        instance.lightOn.setValue(new BooleanResourceValue(false));
//                        instance.opaqueExampleResource.setValue(new OpaqueResourceValue("ABC".getBytes()));
//                        instance.firmwireUpdateResource.setValue(null);
//                        instance.linkExampleResource.setValue(new LinkResourceValue(new Link(1, 1)));
//                        instance.optionalIntegerResource.setValue(new IntegerResourceValue(12));
//                        instance.multipleStringExample.setValues(Arrays.asList(new StringResourceValue("A"), new StringResourceValue("B")));
//                        instance.multipleOptionalStringExample.setValues(null);
//                        client.create(instance, 20);

//
//                        Thread.sleep(2000);
//
//                        obj.getAttributes().minimumPeriod = 5;
//                        obj.getAttributes().maximumPeriod = 5;
//                        obj.writeAttributes();
//                        obj.discover();
//
//                        Thread.sleep(2000);
//                        instance.getAttributes().minimumPeriod = 10;
//                        instance.getAttributes().maximumPeriod = 15;
//                        instance.writeAttributes();
//                        instance.discover();
//
//                        Thread.sleep(2000);
//                        instance.batteryLevel.getAttributes().lessThan = 15.0;
//                        instance.batteryLevel.getAttributes().greaterThan = 10.0;
//                        instance.batteryLevel.getAttributes().maximumPeriod = 60;
//                        instance.batteryLevel.getAttributes().minimumPeriod = 20;
//                        instance.batteryLevel.writeAttributes();
//                        instance.batteryLevel.discover();
//
//                        instance.batteryLevel.observe(r -> {
//                            System.out.println("resource notif" + r.getValue().value);
//                        });


                        ObjectBaseProxy<AndroidUtilsInstanceProxy> obj = client.getObjectTree().getObjectForType(AndroidUtilsInstanceProxy.class);
                        AndroidUtilsInstanceProxy instance = obj.getInstance(0);

                        Thread.sleep(5000);

                        instance.flashlight.read();
                        boolean flash = instance.flashlight.getValue().value;

                        while (client.getStatus() == ClientProxyStatus.REGISTERED) {
                            instance.flashlight.setValue(new BooleanResourceValue(!flash));
                            instance.write();
                            flash = !flash;
                            Thread.sleep(1000);

                            instance.vibrate.execute("");
                            Thread.sleep(1000);
                        }

//                        int value = 0;
//                        while (client.getStatus() == ClientProxyStatus.REGISTERED) {
//                            instance.batteryLevel.read();
////                            instance.write();
//                            Thread.sleep(1000);
//                        }
//                        System.out.println("END");


                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
        });

        server.start(transportConfiguration);


//        Thread.sleep(5000);
//
//        ClientProxy client = server.getClientForEndpointName("lynx_ep");

//        instance.batteryLevel.setValue(new IntegerResourceValue(15));
//        instance.write();
//        Thread.sleep(2000);
//        instance.batteryLevel.setValue(new IntegerResourceValue(16));
//        instance.write();
//        Thread.sleep(2000);
//        instance.batteryLevel.setValue(new IntegerResourceValue(17));
//        instance.write();
//        Thread.sleep(2000);
//        instance.batteryLevel.setValue(new IntegerResourceValue(18));
//        instance.write();
//        Thread.sleep(2000);
//        instance.batteryLevel.setValue(new IntegerResourceValue(19));
//        instance.write();


//        instance.observe(i -> {
//            System.out.println("instance notif" + i.getResource(0).getValue().value);
//        });
//
//        obj.observe(o -> {
//            System.out.println("object notif" + o.getInstance(20).getResource(0).getValue().value);
//        });
//
//        Thread.sleep(2000);
//        instance.batteryLevel.setValue(new IntegerResourceValue(12));
//        instance.write();


//        System.out.println("START CLIENT NOW!");
//        Thread.sleep(7000);
//        System.out.println("-------------------");
//        Thread.sleep(7000);
//        System.out.println("-------------------");
//
//        Thread.sleep(17000);
//        System.out.println("-------------------");
//
//        Thread.sleep(31000);
//        System.out.println("-------------------");


    }

}
