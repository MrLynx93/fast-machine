//package com.test.performance.onroad;
//
//import com.agh.fastmachine.server.api.ClientProxy;
//import com.agh.fastmachine.server.api.Server;
//import com.agh.fastmachine.server.api.ServerConfiguration;
//import com.agh.fastmachine.server.api.listener.RegistrationListener;
//import com.agh.fastmachine.server.bootstrap.BootstrapServer;
//import com.agh.fastmachine.server.internal.transport.mqtt.MqttConfiguration;
//import com.test.performance.AbstractMqttTest;
//import com.test.performance.model.TestInstanceProxy;
//import com.test.util.model.PingInstanceProxy;
//
//import java.util.List;
//
//public class MqttOnroadTest extends AbstractMqttTest {
//    final String LOCAL_BROKER_ADDRESS = "localhost:1883";
//    final String PUBLIC_BROKER_ADDRESS = "ec2-34-250-196-139.eu-west-1.compute.amazonaws.com:1883"; // TODO
//    final String BROKER_ADDRESS = PUBLIC_BROKER_ADDRESS;
//
//    public static void main(String[] args) throws InterruptedException {
//        new MqttOnroadTest().test();
//    }
//
//    @Override
//    public void prepareTest(Server server) {
//        server.setRegistrationListener(new RegistrationListener() {
//
//            @Override
//            public void onRegister(ClientProxy client) {
//                int serverId = Integer.parseInt(server.getName().split("_")[1]);
//                TestInstanceProxy constantInstance = client.getObjectTree().getObjectForType(TestInstanceProxy.class).getInstance(0);
//
//                constantInstance.clientId.read();
//
//            }
//        });
//    }
//
//    @Override
//    public Server configureServer(int number) {
//        ServerConfiguration configuration = new ServerConfiguration();
//        configuration.setTransport(ServerConfiguration.TRASPORT_MQTT);
//        configuration.setName("server_" + number);
//        configuration.addObjectSupport(TestInstanceProxy.class);
//        configuration.addObjectSupport(PingInstanceProxy.class);
//
//        MqttConfiguration transportConfiguration = new MqttConfiguration();
//        transportConfiguration.setBrokerAddress(BROKER_ADDRESS);
//        transportConfiguration.setQos(0);
//        transportConfiguration.setDtls(false);
//        return new Server(configuration, transportConfiguration);
//    }
//
//    @Override
//    public BootstrapServer configureBootstrapServer(List<Server> servers) {
//        MqttConfiguration mqttConfiguration = new MqttConfiguration();
//        mqttConfiguration.setQos(0);
//        mqttConfiguration.setBrokerAddress(BROKER_ADDRESS);
//        mqttConfiguration.setServerName("bootstrap-server");
//        mqttConfiguration.setDtls(false);
//
//        BootstrapServer bootstrapServer = new BootstrapServer(mqttConfiguration);
//        bootstrapServer.setSequenceForPattern(".*", configureBootstrapSequence(servers));
//        return bootstrapServer;
//    }
//}
