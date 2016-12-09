package basic;

import com.agh.fastmachine.core.api.model.resourcevalue.*;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;
import com.agh.fastmachine.server.api.model.builtin.SecurityObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.builtin.ServerObjectInstanceProxy;
import com.agh.fastmachine.server.bootstrap.BootstrapSequence;
import com.agh.fastmachine.server.bootstrap.BootstrapServer;
import com.agh.fastmachine.server.internal.transport.mqtt.MqttConfiguration;
import util.model.ExampleMqttInstanceProxy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BootstrapServerTest {
    private static final String PUBLIC_BROKER_ADDRESS = "tcp://broker.hivemq.com:1883";
    private static final String AMAZON_BROKER_ADDRESS = "tcp://ec2-52-212-253-117.eu-west-1.compute.amazonaws.com:1883";
    private static final String PRIVATE_BROKER_ADDRESS = "tcp://localhost:1883";

    private static final String LOCAL_CLIENT_NAME = "lynx_ep";
    private static final String ANDROID_CLIENT_NAME = "client-android";



    private static final String BROKER_ADDRESS = AMAZON_BROKER_ADDRESS;
    private static final String CLIENT_NAME = ANDROID_CLIENT_NAME;

    private static final String BOOTSTRAP_SERVER_NAME = "lynx-bootstrap-server";
    private static final String SERVER_NAME = "lynx-server";
    private static final int BOOTSTRAP_SERVER_ID = 555;
    private static final int SERVER_ID = 123;

    // TODO access control
    public static void main(String[] args) {
            MqttConfiguration configuration = new MqttConfiguration();
            configuration.setQos(1);
            configuration.setBrokerAddress(BROKER_ADDRESS);
            configuration.setServerId(BOOTSTRAP_SERVER_NAME);

            ExampleMqttInstanceProxy exampleInstance = exampleInstance();

            BootstrapSequence bootstrapSequence = BootstrapSequence.sequenceFor(CLIENT_NAME)
//                    .deleteAll()
                    .writeObject(securityObject())
                    .writeInstance(serverInstance())
                    .writeInstance(bootstrapServerInstance())
//                    .writeInstance(exampleInstance)
                    .finish();
            // TODO bootstrap write resource

            BootstrapServer bootstrapServer = new BootstrapServer(BOOTSTRAP_SERVER_NAME, BOOTSTRAP_SERVER_ID);
            bootstrapServer.setSequenceForClient(CLIENT_NAME, bootstrapSequence);
            bootstrapServer.start(configuration);
    }

    private static ObjectBaseProxy<SecurityObjectInstanceProxy> securityObject() {
        SecurityObjectInstanceProxy bootstrapInstance = new SecurityObjectInstanceProxy(0);
        bootstrapInstance.serverUri.setValue(new StringResourceValue(BOOTSTRAP_SERVER_NAME));
        bootstrapInstance.bootstrapServer.setValue(new BooleanResourceValue(true));
        bootstrapInstance.shortServerId.setValue(new IntegerResourceValue(BOOTSTRAP_SERVER_ID));
//        bootstrapInstance.clientHoldOffTime.setValue(new IntegerResourceValue(120));

        SecurityObjectInstanceProxy serverInstance = new SecurityObjectInstanceProxy(1);
        serverInstance.serverUri.setValue(new StringResourceValue(SERVER_NAME));
        serverInstance.bootstrapServer.setValue(new BooleanResourceValue(false));
        serverInstance.shortServerId.setValue(new IntegerResourceValue(SERVER_ID));
        serverInstance.clientHoldOffTime.setValue(null);

        Map<Integer, ObjectInstanceProxy> instances = new HashMap<>();
        instances.put(bootstrapInstance.getId(), bootstrapInstance);
        instances.put(serverInstance.getId(), serverInstance);

        return new ObjectBaseProxy<>(0, instances);
    }

    private static ObjectInstanceProxy bootstrapServerInstance() {
        SecurityObjectInstanceProxy bootstrapInstance = new SecurityObjectInstanceProxy(0);
        bootstrapInstance.serverUri.setValue(new StringResourceValue(BOOTSTRAP_SERVER_NAME));
        bootstrapInstance.bootstrapServer.setValue(new BooleanResourceValue(true));
        bootstrapInstance.shortServerId.setValue(new IntegerResourceValue(BOOTSTRAP_SERVER_ID));
        bootstrapInstance.clientHoldOffTime.setValue(new IntegerResourceValue(120));
        return bootstrapInstance;
    }

    private static ObjectInstanceProxy serverInstance() {
        ServerObjectInstanceProxy serverInstance = new ServerObjectInstanceProxy(0);
        serverInstance.shortServerId.setValue(new IntegerResourceValue(SERVER_ID));
        serverInstance.lifetime.setValue(new IntegerResourceValue(20));
        serverInstance.defaultMinimumPeriod.setValue(new IntegerResourceValue(10));
        serverInstance.defaultMaximumPeriod.setValue(new IntegerResourceValue(30));
        serverInstance.storeNotifications.setValue(new BooleanResourceValue(false));
        serverInstance.binding.setValue(new StringResourceValue("T"));
        return serverInstance;
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
}
