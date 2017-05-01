package com.test.server;

import com.agh.fastmachine.client.api.Client;
import com.agh.fastmachine.client.api.CoapClientConf;
import com.agh.fastmachine.client.api.model.ObjectBase;
import com.agh.fastmachine.client.api.model.builtin.SecurityObjectBase;
import com.agh.fastmachine.client.api.model.builtin.SecurityObjectInstance;
import com.agh.fastmachine.client.api.model.builtin.ServerObjectBase;
import com.agh.fastmachine.client.api.model.builtin.ServerObjectInstance;
import com.agh.fastmachine.core.api.model.resourcevalue.BooleanResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.IntegerResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.StringResourceValue;
import com.agh.fastmachine.server.api.ClientProxy;
import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.ServerConfiguration;
import com.agh.fastmachine.server.api.listener.RegistrationListener;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.builtin.AccessControlObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.builtin.SecurityObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.builtin.ServerObjectInstanceProxy;
import com.agh.fastmachine.server.bootstrap.BootstrapSequence;
import com.agh.fastmachine.server.bootstrap.BootstrapServer;
import com.agh.fastmachine.server.internal.transport.coap.CoapConfiguration;
import com.agh.fastmachine.server.internal.transport.mqtt.MqttConfiguration;
import com.test.performance.model.TestInstanceProxy;
import com.test.performance.model.TestObjectBase;
import com.test.performance.model.TestObjectInstance;

import java.util.*;
import java.util.concurrent.CountDownLatch;

public class Utils {
    public static final int CREATE = 1 << 4;
    public static final int READ = 1;
    public static final int WRITE = 1 << 1;
    public static final int EXECUTE = 1 << 2;
    public static final int DELETE = 1 << 3;
    public static final int LIFETIME = 3600;

//    public static final String SERVER_IP = "localhost";
    public static final String SERVER_IP = "34.252.159.36";

    public static RegistrationListener counterListener(CountDownLatch registerCount, CountDownLatch deregisterCount) {
        return new RegistrationListener() {
            @Override
            public void onRegister(ClientProxy client) {
                registerCount.countDown();
            }

            @Override
            public void onDeregister(ClientProxy client) {
                deregisterCount.countDown();
            }
        };
    }

    public static class MQTT {
        public static final int SERVER_ID = 1;
        private static final String BROKER_ADDRESS = "34.250.196.139";
        public static final String SERVER_NAME = "test_mqtt_server";
        private static final int QoS = 1;

        public static BootstrapServer configureBootstrapServer(boolean tls) {
            MqttConfiguration mqttConfiguration = new MqttConfiguration();
            mqttConfiguration.setQos(QoS);
            mqttConfiguration.setBrokerAddress(BROKER_ADDRESS + (tls ? ":8883" : ":1883"));
            mqttConfiguration.setServerName("bootstrap-server");
            mqttConfiguration.setDtls(tls);
            mqttConfiguration.setKeyStoreLocation("ca.jks");
            mqttConfiguration.setKeyStorePassword("123456");

            BootstrapServer bootstrapServer = new BootstrapServer(mqttConfiguration);
            bootstrapServer.setSequenceForPattern(".*", configureBootstrapSequence());
            return bootstrapServer;
        }

        public static Server configureServer(boolean tls) {
            ServerConfiguration configuration = new ServerConfiguration();
            configuration.setTransport(ServerConfiguration.TRASPORT_MQTT);
            configuration.setName(SERVER_NAME);
            configuration.addObjectSupport(TestInstanceProxy.class);

            MqttConfiguration transportConfiguration = new MqttConfiguration();
            transportConfiguration.setBrokerAddress(BROKER_ADDRESS + (tls ? ":8883" : ":1883"));
            transportConfiguration.setQos(QoS);
            transportConfiguration.setDtls(tls);
            transportConfiguration.setKeyStoreLocation("ca.jks");
            transportConfiguration.setKeyStorePassword("123456");
            return new Server(configuration, transportConfiguration);
        }

        private static BootstrapSequence configureBootstrapSequence() {
            return new BootstrapSequence()
                    .writeInstance(bootstrapSecurityInstance())
                    .writeObject(accessControlObject())
                    .writeInstance(securityInstance())
                    .writeInstance(serverInstance())
                    .finish();
        }

        private static ObjectBaseProxy accessControlObject() {
            Map<Integer, AccessControlObjectInstanceProxy> instances = new HashMap<>();

            // AC for object
            AccessControlObjectInstanceProxy testObjectAccess = new AccessControlObjectInstanceProxy(0);
            testObjectAccess.objectId.setValue(new IntegerResourceValue(20004));
            testObjectAccess.instanceId.setValue(new IntegerResourceValue(66535));
            testObjectAccess.accessControlOwner.setValue(new IntegerResourceValue(66535));
            testObjectAccess.accessControlList.setValue(new IntegerResourceValue(CREATE), 0);
            instances.put(0, testObjectAccess);

            // AC for general instance - only read allowed for everyone
            AccessControlObjectInstanceProxy testGeneralInstanceAccess = new AccessControlObjectInstanceProxy(1);
            testGeneralInstanceAccess.objectId.setValue(new IntegerResourceValue(20004));
            testGeneralInstanceAccess.instanceId.setValue(new IntegerResourceValue(0));
            testGeneralInstanceAccess.accessControlOwner.setValue(new IntegerResourceValue(66535));
            testGeneralInstanceAccess.accessControlList.setValue(new IntegerResourceValue(READ), 0);
            instances.put(1, testGeneralInstanceAccess);

            return new ObjectBaseProxy(2, instances);
        }

        private static SecurityObjectInstanceProxy bootstrapSecurityInstance() {
            SecurityObjectInstanceProxy inst = new SecurityObjectInstanceProxy(0);
            inst.serverUri.setValue(new StringResourceValue("bootstrap-server"));
            inst.bootstrapServer.setValue(new BooleanResourceValue(true));
            inst.shortServerId.setValue(new IntegerResourceValue(0));
            inst.clientHoldOffTime.setValue(new IntegerResourceValue(60));
            return inst;
        }

        private static SecurityObjectInstanceProxy securityInstance() {
            SecurityObjectInstanceProxy inst = new SecurityObjectInstanceProxy(SERVER_ID);
            inst.serverUri.setValue(new StringResourceValue("test_mqtt_server"));
            inst.bootstrapServer.setValue(new BooleanResourceValue(false));
            inst.shortServerId.setValue(new IntegerResourceValue(SERVER_ID));
            return inst;
        }

        private static ServerObjectInstanceProxy serverInstance() {
            ServerObjectInstanceProxy inst = new ServerObjectInstanceProxy(SERVER_ID);
            inst.shortServerId.setValue(new IntegerResourceValue(SERVER_ID));
            inst.storeNotifications.setValue(new BooleanResourceValue(false));
            inst.lifetime.setValue(new IntegerResourceValue(LIFETIME));
            inst.binding.setValue(new StringResourceValue("T"));
            return inst;
        }
    }

    public static class CoAP {
        public static final String SERVER_NAME = "test_coap_server";
        public static final int SERVER_ID = 1;

        public static Server configureServer(boolean dtls) {
            ServerConfiguration configuration = new ServerConfiguration();
            configuration.setTransport(ServerConfiguration.TRASPORT_COAP);
            configuration.setName(SERVER_NAME);
            configuration.addObjectSupport(TestInstanceProxy.class);

            CoapConfiguration transportConfiguration = new CoapConfiguration();
            transportConfiguration.setPort(19001);
            transportConfiguration.setDtls(dtls);
            transportConfiguration.setKeyStorePassword("123456");
            transportConfiguration.setKeyStoreLocation("keyStore.jks");
            transportConfiguration.setTrustStorePassword("123456");
            transportConfiguration.setTrustStoreLocation("trustStore.jks");
            return new Server(configuration, transportConfiguration);
        }

        public static Client configureClient(int clientIdx, boolean dtls, CountDownLatch operationsCount) {
            CoapClientConf configuration = new CoapClientConf();
            configuration.setPort(29000 + clientIdx);
            configuration.setDtls(dtls);
            configuration.setKeyStorePassword("123456");
            configuration.setKeyStoreLocation("keyStore.jks");
            configuration.setTrustStorePassword("123456");
            configuration.setTrustStoreLocation("trustStore.jks");
            return new Client("client_" + (29000 + clientIdx), factoryBootstrap(clientIdx, operationsCount), configuration);
        }

        private static List<ObjectBase<?>> factoryBootstrap(int clientIdx, CountDownLatch operationsCount) {
            ServerObjectBase serverBase = new ServerObjectBase();
            SecurityObjectBase securityBase = new SecurityObjectBase();
            TestObjectBase testBase = new TestObjectBase();

            ServerObjectInstance serverInst = serverBase.getNewInstance(1);
            serverInst.shortServerId.setValue(new IntegerResourceValue(SERVER_ID));
            serverInst.lifetime.setValue(new IntegerResourceValue(LIFETIME));
            serverInst.binding.setValue(new StringResourceValue("U"));

            SecurityObjectInstance securityInst = securityBase.getNewInstance(SERVER_ID);
            securityInst.shortServerId.setValue(new IntegerResourceValue(SERVER_ID));
            securityInst.bootstrapServer.setValue(new BooleanResourceValue(false));
            securityInst.serverUri.setValue(new StringResourceValue("coap://" + SERVER_IP + ":19001"));

            TestObjectInstance testInstance = testBase.getNewInstance(0);
            testInstance.clientId.setValue(new StringResourceValue("client_" + (29000 + clientIdx)));
            testInstance.serverId.setValue(new StringResourceValue("global"));
            testInstance.payload.setValue(new StringResourceValue("ABC"));
            testInstance.setCounter(operationsCount);

            return Arrays.asList(serverBase, securityBase, testBase);
        }
    }
}