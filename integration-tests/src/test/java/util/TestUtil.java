package util;

import com.agh.fastmachine.client.api.Client;
import com.agh.fastmachine.client.api.model.ObjectBase;
import com.agh.fastmachine.client.api.model.builtin.SecurityObjectBase;
import com.agh.fastmachine.client.api.model.builtin.SecurityObjectInstance;
import com.agh.fastmachine.client.api.model.builtin.ServerObjectBase;
import com.agh.fastmachine.client.api.model.builtin.ServerObjectInstance;
import com.agh.fastmachine.core.api.model.resourcevalue.*;
import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.ServerConfiguration;
import org.eclipse.californium.core.network.CoapEndpoint;
import util.model.ExampleObjectBase;
import util.model.ExampleObjectInstance;
import util.model.ExampleObjectInstanceProxy;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class TestUtil {
    public static final int EXMAPLE_SERVER_PORT = 3334;
    public static final int EXAMPLE_OBJECT_ID = 1789;

    public static Server startExampleServer() {
        Server server = new Server();
        server.setConfiguration(createExampleServerConfiguration());
        server.start();
        return server;
    }

    public static Client startExampleClient(String endpointClientName) {
        return startExampleClient(endpointClientName, "coap://localhost:" + EXMAPLE_SERVER_PORT);
    }


    public static Client startExampleClient(String endpointClientName, String serverUri) {
        Client client = new Client(endpointClientName, createExampleClientConfiguration(serverUri), new CoapEndpoint(5683));
        client.start();
        return client;
    }

    public static ServerConfiguration createExampleServerConfiguration() {
        ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setPort(EXMAPLE_SERVER_PORT);
        serverConfiguration.addObjectSupport(ExampleObjectInstanceProxy.class);
        return serverConfiguration;
    }

    public static void main(String[] args) {
        ServerObjectBase serverObjectBase = new ServerObjectBase();
        ServerObjectInstance instance = new ServerObjectInstance(5);
        serverObjectBase.addInstance(instance);
    }

    public static List<ObjectBase<?>> createExampleClientConfiguration(String serverUri) {
        ServerObjectBase serverBase = new ServerObjectBase();
        ServerObjectInstance exampleServer = serverBase.getNewInstance();
        exampleServer.shortServerId.setValue(new IntegerResourceValue(66535));
        exampleServer.lifetime.setValue(new IntegerResourceValue(60 * 10));

        SecurityObjectBase securityBase = new SecurityObjectBase();
        SecurityObjectInstance exampleServerSecurity = securityBase.getNewInstance();
        exampleServerSecurity.shortServerId.setValue(new IntegerResourceValue(66535));
        exampleServerSecurity.bootstrapServer.setValue(new BooleanResourceValue(true));
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

        return Arrays.asList(serverBase, securityBase, exampleBase);
    }

    public static void waitSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
