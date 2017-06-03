package com.test.lifetime;

import com.agh.fastmachine.core.api.model.resourcevalue.*;
import com.agh.fastmachine.server.api.listener.BootstrapListener;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.builtin.SecurityObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.builtin.ServerObjectInstanceProxy;
import com.agh.fastmachine.server.bootstrap.BootstrapSequence;
import com.agh.fastmachine.server.bootstrap.BootstrapServer;
import com.agh.fastmachine.server.internal.transport.mqtt.MqttConfiguration;
import com.test.model.test.TestInstanceProxy;
import com.test.util.model.ExampleMqttInstanceProxy;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

public class StartBootstrapServer {
    private static final boolean TLS = false;
    private static final int LIFETIME = 10;
    private static final String BROKER_ADDRESS = "34.250.196.139:1883"; //TODO
    private static final String BOOTSTRAP_SERVER_NAME = "bootstrap-server";
    private static final List<ServerData> androidServers = new ArrayList<>();
    private static final List<ServerData> localServers = new ArrayList<>();

    static {
        /* Don't use 0 as shortServerId. main_server_1 is server for both android and local clients **/
        androidServers.add(new ServerData(1, "main_server_1"));
        localServers.add(new ServerData(1, "main_server_1"));
    }

    public static void main(String[] args) {
        BootstrapServer bootstrapServer = new BootstrapServer(configureTransport());
        configureBootstrapSequence(bootstrapServer);
        bootstrapServer.setBootstrapListener(bootstrapListener);
        bootstrapServer.start();
    }

    private static MqttConfiguration configureTransport() {
        MqttConfiguration configuration = new MqttConfiguration();
        configuration.setBrokerAddress(BROKER_ADDRESS);
        configuration.setServerName("bootstrap-server");
        configuration.setQos(1);
        if (TLS) {
            configuration.setDtls(true);
            configuration.setKeyStoreLocation("ca.jks");
            configuration.setKeyStorePassword("123456");
        }
        return configuration;
    }

    private static void configureBootstrapSequence(BootstrapServer bootstrapServer) {
        bootstrapServer.setSequenceForPattern(".*android.*", sequenceForAndroid());
        bootstrapServer.setSequenceForPattern(".*local.*", sequenceForLocal());
    }

    private static SecurityObjectInstanceProxy bootstrapSecurityInstance() {
        SecurityObjectInstanceProxy inst = new SecurityObjectInstanceProxy(0);
        inst.serverUri.setValue(new StringResourceValue(BOOTSTRAP_SERVER_NAME));
        inst.shortServerId.setValue(new IntegerResourceValue(0));
        inst.bootstrapServer.setValue(new BooleanResourceValue(true));
        return inst;
    }

    private static ObjectBaseProxy<ServerObjectInstanceProxy> prepareServerObject(List<ServerData> servers) {
        Map<Integer, ObjectInstanceProxy> serverInstances = new HashMap<>();
        for (ServerData server : servers) {
            ServerObjectInstanceProxy inst = new ServerObjectInstanceProxy(server.shortId);
            inst.shortServerId.setValue(new IntegerResourceValue(server.shortId));
            inst.binding.setValue(new StringResourceValue("T"));
            inst.lifetime.setValue(new IntegerResourceValue(LIFETIME));
            inst.storeNotifications.setValue(new BooleanResourceValue(false));
            serverInstances.put(server.shortId, inst);
        }
        return new ObjectBaseProxy<>(1, serverInstances);
    }

    private static ObjectBaseProxy<SecurityObjectInstanceProxy> prepareSecurityObject(List<ServerData> servers) {
        Map<Integer, ObjectInstanceProxy> securityInstances = new HashMap<>();
        for (ServerData server : servers) {
            SecurityObjectInstanceProxy inst = new SecurityObjectInstanceProxy(server.shortId);
            inst.shortServerId.setValue(new IntegerResourceValue(server.shortId));
            inst.serverUri.setValue(new StringResourceValue(server.name));
            inst.bootstrapServer.setValue(new BooleanResourceValue(false));
            securityInstances.put(server.shortId, inst);
        }
        return new ObjectBaseProxy<>(0, securityInstances);
    }

    /////////////////////////////ANDROID BOOTSTRAP //////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////

    private static BootstrapSequence sequenceForAndroid() {
        return new BootstrapSequence()
                .writeObject(prepareServerObject(androidServers))
                .writeObject(prepareSecurityObject(androidServers))
                .writeInstance(bootstrapSecurityInstance())
                .finish();
    }

    //////////////////////////// LOCAL BOOTSTRAP //////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////

    private static BootstrapSequence sequenceForLocal() {
        return new BootstrapSequence()
                .writeObject(prepareServerObject(localServers))
                .writeObject(prepareSecurityObject(localServers))
                .writeInstance(bootstrapSecurityInstance())
                .writeInstance(testInstance())
                .finish(); // TODO TEST OBJECTS ETC.
    }

    private static ExampleMqttInstanceProxy exampleInstance() {
        byte[] opaqueValue = "opaque-value".getBytes();

        ExampleMqttInstanceProxy exampleInstance = new ExampleMqttInstanceProxy(1);
        exampleInstance.batteryLevel.setValue(new IntegerResourceValue(80));
        exampleInstance.doubleExampleResource.setValue(new DoubleResourceValue(0.1));
        exampleInstance.stringExampleResource.setValue(new StringResourceValue("example-string"));
        exampleInstance.lightOn.setValue(new BooleanResourceValue(false));
        exampleInstance.opaqueExampleResource.setValue(new OpaqueResourceValue(opaqueValue));
        exampleInstance.firmwireUpdateResource.setValue(null);
        exampleInstance.linkExampleResource.setValue(new LinkResourceValue(new Link(0, 0))); // TODO LINK VALUE
        exampleInstance.optionalIntegerResource.setValue(null);
        exampleInstance.multipleStringExample.setValues(Arrays.asList(new StringResourceValue("res1"), new StringResourceValue("res2")));
        exampleInstance.multipleOptionalStringExample.setValues(null);
        return exampleInstance;
    }

    private static TestInstanceProxy testInstance() {
        TestInstanceProxy testInstanceProxy = new TestInstanceProxy();
        testInstanceProxy.clientId.setValue(new IntegerResourceValue(0));
        testInstanceProxy.serverId.setValue(new IntegerResourceValue(0));
        testInstanceProxy.payload.setValue(new StringResourceValue("ABC"));
        return testInstanceProxy;
    }


    /////////////////////////////////// OTHER /////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////

    private static final BootstrapListener bootstrapListener = new BootstrapListener() {
        @Override
        public void onBootstrapFinish(String clientId) {
            System.out.println("Bootstrapped client " + clientId);
        }
    };

    @Data
    @AllArgsConstructor
    private static class ServerData {
        private int shortId;
        private String name;
    }
}
