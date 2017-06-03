package com.test.util;

import com.agh.fastmachine.client.api.Client;
import com.agh.fastmachine.client.api.CoapClientConf;
import com.agh.fastmachine.client.api.model.ObjectBase;
import com.agh.fastmachine.client.api.model.builtin.SecurityObjectBase;
import com.agh.fastmachine.client.api.model.builtin.SecurityObjectInstance;
import com.agh.fastmachine.client.api.model.builtin.ServerObjectBase;
import com.agh.fastmachine.client.api.model.builtin.ServerObjectInstance;
import com.agh.fastmachine.core.api.model.resourcevalue.*;
import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.ServerConfiguration;
import com.agh.fastmachine.server.internal.transport.coap.CoapConfiguration;
import com.test.model.test.TestInstanceProxy;
import com.test.model.test.TestObjectBase;
import com.test.model.test.TestObjectInstance;
import com.test.util.model.ExampleObjectBase;
import com.test.util.model.ExampleObjectInstance;
import com.test.util.model.ExampleObjectInstanceProxy;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class TestUtil {
    public static final int EXMAPLE_SERVER_PORT = 48411;
    public static final int EXAMPLE_OBJECT_ID = 1789;

    public static CoapClientConf getClientConf() {
        // TODO
        CoapClientConf conf = new CoapClientConf();
        conf.setDtls(false);
        conf.setPort(29001);
//        conf.setKeyStorePassword("123456");
//        conf.setKeyStoreLocation("keyStore.jks");
//        conf.setTrustStorePassword("123456");
//        conf.setTrustStoreLocation("trustStore.jks");
        return conf;
//        return null;
    }

    public static CoapConfiguration getServerConf() {
        // TODO
//        CoapConfiguration conf = new CoapConfiguration();
//        conf.setDtls(true);
//        conf.setPort(48411);
//        conf.setKeyStorePassword("123456");
//        conf.setKeyStoreLocation("keyStore.jks");
//        conf.setTrustStorePassword("123456");
//        conf.setTrustStoreLocation("trustStore.jks");
//        return conf;
        return null;
    }


    public static Server startExampleServer() {
        Server server = new Server(createExampleServerConfiguration(), getServerConf());
        server.start();
        return server;
    }

    public static Client startExampleClient(String endpointClientName) {
        return startExampleClient(endpointClientName, "coap://localhost:" + EXMAPLE_SERVER_PORT);
    }


    public static Client startExampleClient(String endpointClientName, String serverUri) {
        Client client = new Client(endpointClientName, createExampleClientConfiguration(serverUri), getClientConf());
        client.start();
        return client;
    }

    public static ServerConfiguration createExampleServerConfiguration() {
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setPort(EXMAPLE_SERVER_PORT);
        serverConfiguration.addObjectSupport(ExampleObjectInstanceProxy.class);
        serverConfiguration.addObjectSupport(TestInstanceProxy.class);
        return serverConfiguration;
    }

    public static void main(String[] args) {
        ServerObjectBase serverObjectBase = new ServerObjectBase();
        ServerObjectInstance instance = new ServerObjectInstance(5);
        serverObjectBase.addInstance(instance);
    }
    static List<ObjectBase<?>> factoryBootstrap(String serverUri) {
        ServerObjectBase serverBase = new ServerObjectBase();
        SecurityObjectBase securityBase = new SecurityObjectBase();
        TestObjectBase testBase = new TestObjectBase();

//        for (Integer serverId : servers) {
            ServerObjectInstance serverInst = serverBase.getNewInstance(1);
            serverInst.shortServerId.setValue(new IntegerResourceValue(1));
            serverInst.lifetime.setValue(new IntegerResourceValue(123));
            serverInst.binding.setValue(new StringResourceValue("U"));

            SecurityObjectInstance securityInst = securityBase.getNewInstance(1);
            securityInst.shortServerId.setValue(new IntegerResourceValue(1));
            securityInst.bootstrapServer.setValue(new BooleanResourceValue(false));
            securityInst.serverUri.setValue(new StringResourceValue(serverUri));
//        }

        TestObjectInstance testInstance = testBase.getNewInstance(0);
        testInstance.clientId.setValue(new StringResourceValue("client_" + (29000 + 12)));
        testInstance.serverId.setValue(new StringResourceValue("global"));
        testInstance.payload.setValue(new StringResourceValue("ABC"));
//        testInstance.setCounter(counter);
//        ExampleObjectBase exampleBase = new ExampleObjectBase();

        return Arrays.asList(serverBase, securityBase, testBase);
    }

    public static List<ObjectBase<?>> createExampleClientConfiguration(String serverUri) {
        ServerObjectBase serverBase = new ServerObjectBase();
        ServerObjectInstance exampleServer = serverBase.getNewInstance();
        exampleServer.shortServerId.setValue(new IntegerResourceValue(66535));
        exampleServer.lifetime.setValue(new IntegerResourceValue(10));

        SecurityObjectBase securityBase = new SecurityObjectBase();
        SecurityObjectInstance exampleServerSecurity = securityBase.getNewInstance();
        exampleServerSecurity.shortServerId.setValue(new IntegerResourceValue(66535));
        exampleServerSecurity.bootstrapServer.setValue(new BooleanResourceValue(false));
        exampleServerSecurity.serverUri.setValue(new StringResourceValue(serverUri));

        ExampleObjectBase exampleBase = new ExampleObjectBase();
        ExampleObjectInstance exampleInstance1 = exampleBase.getNewInstance(1);
        ExampleObjectInstance exampleInstance2 = exampleBase.getNewInstance(4);
        exampleInstance1.floatResource.setValue(new FloatResourceValue(1.1f));
        exampleInstance1.stringResource.setValue(new StringResourceValue("Hello, instance 1 :)"));
        exampleInstance1.dateResource.setValue(new DateResourceValue(new Date(10, 8, 26)));
        exampleInstance1.integerResource.setValue(new IntegerResourceValue(4));
        exampleInstance1.multipleIntegerResource.setValue(new IntegerResourceValue(5555, 0));
        exampleInstance1.multipleIntegerResource.setValue(new IntegerResourceValue(5555, 1));

        exampleInstance2.floatResource.setValue(new FloatResourceValue(3.5f));
        exampleInstance2.stringResource.setValue(new StringResourceValue("Hello, instance 4 :)"));
        exampleInstance2.dateResource.setValue(new DateResourceValue(new Date()));
        exampleInstance2.integerResource.setValue(new IntegerResourceValue(2));

        TestObjectBase testObjectBase = new TestObjectBase();


        return Arrays.asList(serverBase, securityBase, exampleBase, testObjectBase);
    }

    public static void waitSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
