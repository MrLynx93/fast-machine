package com.test.lifetime;

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
import com.test.model.test.TestObjectBase;
import com.test.model.test.TestObjectInstance;

import java.util.Arrays;
import java.util.List;

public class StartCoapClient {
    private static final String SERVER_IP = "34.252.159.36";
    private static final int SERVER_ID = 1;
    private static Client client;
    private static int lifetime;

    public static void main(String[] args) {
        lifetime = Integer.parseInt(args[0]);
        client = configureClient();
        client.start();
    }


    public static Client configureClient() {
        CoapClientConf configuration = new CoapClientConf();
        configuration.setPort(29001);
        configuration.setDtls(false);
        configuration.setKeyStorePassword("123456");
        configuration.setKeyStoreLocation("keyStore.jks");
        configuration.setTrustStorePassword("123456");
        configuration.setTrustStoreLocation("trustStore.jks");
        return new Client("client_android", factoryBootstrap(), configuration);
    }

    private static List<ObjectBase<?>> factoryBootstrap() {
        ServerObjectBase serverBase = new ServerObjectBase();
        SecurityObjectBase securityBase = new SecurityObjectBase();
        TestObjectBase testBase = new TestObjectBase();

        ServerObjectInstance serverInst = serverBase.getNewInstance(1);
        serverInst.shortServerId.setValue(new IntegerResourceValue(SERVER_ID));
        serverInst.lifetime.setValue(new IntegerResourceValue(lifetime));
        serverInst.binding.setValue(new StringResourceValue("U"));

        SecurityObjectInstance securityInst = securityBase.getNewInstance(SERVER_ID);
        securityInst.shortServerId.setValue(new IntegerResourceValue(SERVER_ID));
        securityInst.bootstrapServer.setValue(new BooleanResourceValue(false));
        securityInst.serverUri.setValue(new StringResourceValue("coap://" + SERVER_IP + ":19001"));

        TestObjectInstance testInstance = testBase.getNewInstance(0);
        testInstance.clientId.setValue(new StringResourceValue("asd"));
        testInstance.serverId.setValue(new StringResourceValue("global"));
        testInstance.payload.setValue(new StringResourceValue("ABC"));

        return Arrays.asList(serverBase, securityBase, testBase);
    }

}
