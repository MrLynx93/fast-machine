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
import util.model.PingInstanceProxy;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MqttTest {
    private static final String PUBLIC_BROKER_ADDRESS = "tcp://broker.hivemq.com:1883";
    private static final String AMAZON_BROKER_ADDRESS = "tcp://ec2-52-212-253-117.eu-west-1.compute.amazonaws.com:1883";
    private static final String PRIVATE_BROKER_ADDRESS = "tcp://localhost:1883";


    private static String BROKER_ADDRESS = PRIVATE_BROKER_ADDRESS;

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
        configuration.addObjectSupport(PingInstanceProxy.class);

        configuration.setTransport(ServerConfiguration.TRASPORT_MQTT);

        server.setConfiguration(configuration);

        server.setRegistrationListener(new RegistrationListener() {
            @Override
            public void onDeregister(ClientProxy client) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        server.stop();

                    }
                });
            }
        });
//        server.setRegistrationListener(new RegistrationListener() {
//            @Override
//            public void onRegister(ClientProxy client) {
//
//            }
//            );
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
