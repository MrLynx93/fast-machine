package interfaces.observe;

import com.agh.fastmachine.client.api.Client;
import com.agh.fastmachine.core.api.model.Attributes;
import com.agh.fastmachine.core.api.model.resourcevalue.FloatResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.StringResourceValue;
import com.agh.fastmachine.server.api.ClientProxy;
import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.listener.ObservationListener;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;
import com.agh.fastmachine.server.api.model.ObjectTree;
import com.agh.fastmachine.server.internal.client.ClientProxyStatus;
import org.hamcrest.Matchers;
import org.junit.Test;
import util.model.ExampleObjectInstance;
import util.model.ExampleObjectInstanceProxy;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static util.TestUtil.*;

public class ObserveIT {
    private static final String ENDPOINT_CLIENT_NAME = "132:123:123";
    public static final int EXAMPLE_OBJECT_ID = 1789;
    private ObjectBaseProxy<ExampleObjectInstanceProxy> exampleObjectProxy;
    private ExampleObjectInstanceProxy exampleInstanceProxy;
    private ExampleObjectInstance exampleInstance;
    private int gotNotifications = 0;

    @Test
    public void shouldObserveObject() throws Exception {
        gotNotifications = 0;
        // Create Server and Client with Factory bootstrap
        // Register is done inside starting Client
        Server server = startExampleServer();
        Client client = startExampleClient(ENDPOINT_CLIENT_NAME);
        ClientProxy clientProxy = server.getClientForEndpointName(ENDPOINT_CLIENT_NAME);

        ObjectTree objectTree = clientProxy.getObjectTree();
        exampleObjectProxy = objectTree.getObjectForType(ExampleObjectInstanceProxy.class);
        exampleInstanceProxy = exampleObjectProxy.getInstance(1);
        exampleInstance = (ExampleObjectInstance) client.getObjectBaseMap().get(EXAMPLE_OBJECT_ID).getInstance(1);

        // Read first value
        exampleInstanceProxy.stringResource.read();
        assertThat(exampleInstanceProxy.stringResource.getValue().value, equalTo("Hello, instance 1 :)"));

        // Set write attributes
        final Attributes attributes = exampleObjectProxy.getAttributes();
        attributes.maximumPeriod = 5;
        attributes.minimumPeriod = 2;
        exampleObjectProxy.writeAttributes();

        // Start observing
        final long startTime = System.currentTimeMillis();
        exampleObjectProxy.observe(new ObservationListener<ObjectBaseProxy<ExampleObjectInstanceProxy>>() {
            @Override
            public void onNotify(ObjectBaseProxy<ExampleObjectInstanceProxy> node) {
                gotNotifications++;
                assertThat(node.getInstance(1).stringResource.getValue().value, Matchers.equalTo("New Value"));
            }
        });

        exampleInstance.stringResource.setValue(new StringResourceValue("New Value"));
        assertThat(gotNotifications, Matchers.equalTo(0));

        // Check if you got notify
        waitSeconds(attributes.minimumPeriod); // wait for 1st notify
        waitSeconds(attributes.maximumPeriod); // wait for 2nd notify
        assertThat(gotNotifications, Matchers.equalTo(1));
        assertThat(exampleInstanceProxy.stringResource.getValue().value, Matchers.equalTo("New Value"));
    }

    @Test
    public void shouldObserveObjectInstance() throws Exception {
        gotNotifications = 0;
        // Create Server and Client with Factory bootstrap
        // Register is done inside starting Client
        Server server = startExampleServer();
        Client client = startExampleClient(ENDPOINT_CLIENT_NAME);
        ClientProxy clientProxy = server.getClientForEndpointName(ENDPOINT_CLIENT_NAME);

        ObjectTree objectTree = clientProxy.getObjectTree();
        exampleObjectProxy = objectTree.getObjectForType(ExampleObjectInstanceProxy.class);
        exampleInstanceProxy = exampleObjectProxy.getInstance(1);
        exampleInstance = (ExampleObjectInstance) client.getObjectBaseMap().get(EXAMPLE_OBJECT_ID).getInstance(1);

        // Read first value
        exampleInstanceProxy.stringResource.read();
        assertThat(exampleInstanceProxy.stringResource.getValue().value, equalTo("Hello, instance 1 :)"));

        // Set write attributes
        final Attributes attributes = exampleInstanceProxy.getAttributes();
        attributes.maximumPeriod = 5;
        attributes.minimumPeriod = 2;
        exampleInstanceProxy.writeAttributes();

        // Start observing
        final long startTime = System.currentTimeMillis();
        exampleInstanceProxy.observe(new ObservationListener<ExampleObjectInstanceProxy>() {
            @Override
            public void onNotify(ExampleObjectInstanceProxy node) {
                gotNotifications++;
                assertThat(node.stringResource.getValue().value, Matchers.equalTo("New Value"));
            }
        });

        exampleInstance.stringResource.setValue(new StringResourceValue("New Value"));
        assertThat(gotNotifications, Matchers.equalTo(0));

        // Check if you got notify
        waitSeconds(attributes.minimumPeriod); // wait for 1st notify
        waitSeconds(attributes.maximumPeriod); // wait for 2nd notify
        assertThat(gotNotifications, Matchers.equalTo(1));
        assertThat(exampleInstanceProxy.stringResource.getValue().value, Matchers.equalTo("New Value"));
    }

    @Test
    public void shouldObserveNotNumericResource() throws Exception {
        gotNotifications = 0;
        // Create Server and Client with Factory bootstrap
        // Register is done inside starting Client
        Server server = startExampleServer();
        Client client = startExampleClient(ENDPOINT_CLIENT_NAME);
        ClientProxy clientProxy = server.getClientForEndpointName(ENDPOINT_CLIENT_NAME);

        ObjectTree objectTree = clientProxy.getObjectTree();
        exampleObjectProxy = objectTree.getObjectForType(ExampleObjectInstanceProxy.class);
        exampleInstanceProxy = exampleObjectProxy.getInstance(1);
        exampleInstance = (ExampleObjectInstance) client.getObjectBaseMap().get(EXAMPLE_OBJECT_ID).getInstance(1);

        // Read first value
        exampleInstanceProxy.stringResource.read();
        assertThat(exampleInstanceProxy.stringResource.getValue().value, equalTo("Hello, instance 1 :)"));

        // Set write attributes
        final Attributes attributes = exampleInstanceProxy.stringResource.getAttributes();
        attributes.maximumPeriod = 5;
        attributes.minimumPeriod = 2;
        exampleInstanceProxy.stringResource.writeAttributes();

        // Start observing
        final long startTime = System.currentTimeMillis();
        exampleInstanceProxy.stringResource.observe(new ObservationListener<ObjectResourceProxy<StringResourceValue>>() {
            @Override
            public void onNotify(ObjectResourceProxy<StringResourceValue> node) {
                gotNotifications++;
                assertThat(node.getValue().value, Matchers.equalTo("New Value"));
            }
        });

        exampleInstance.stringResource.setValue(new StringResourceValue("New Value"));
        assertThat(gotNotifications, Matchers.equalTo(0));

        // Check if you got notify
        waitSeconds(attributes.maximumPeriod); // wait for notify
        assertThat(gotNotifications, Matchers.equalTo(1));
        assertThat(exampleInstanceProxy.stringResource.getValue().value, Matchers.equalTo("New Value"));
    }

    @Test
    public void shouldCancelObservation() throws Exception {
        // Create Server and Client with Factory bootstrap
        // Register is done inside starting Client
        Server server = startExampleServer();
        Client client = startExampleClient(ENDPOINT_CLIENT_NAME);
        ClientProxy clientProxy = server.getClientForEndpointName(ENDPOINT_CLIENT_NAME);

        assertThat(clientProxy.getStatus(), equalTo(ClientProxyStatus.REGISTERED));

        final ObjectTree objectTree = clientProxy.getObjectTree();
        exampleObjectProxy = objectTree.getObjectForType(ExampleObjectInstanceProxy.class);
        exampleInstanceProxy = exampleObjectProxy.getInstance(1);
        exampleInstance = (ExampleObjectInstance) client.getObjectBaseMap().get(EXAMPLE_OBJECT_ID).getInstance(1);

        // Read first value
        exampleInstanceProxy.floatResource.read();
        assertThat(exampleInstanceProxy.floatResource.getValue().value, equalTo(1.1f));

        // Set write attributes
        Attributes attributes = exampleInstanceProxy.floatResource.getAttributes();
        attributes.maximumPeriod = 5;
        attributes.minimumPeriod = 2;
        exampleInstanceProxy.floatResource.writeAttributes();

        // Start observing
        exampleInstanceProxy.floatResource.observe(new ObservationListener<ObjectResourceProxy<FloatResourceValue>>() {
            @Override
            public void onNotify(ObjectResourceProxy<FloatResourceValue> node) {
                gotNotifications++;
                assertThat(node.getValue().value, greaterThan(2.0f));
                exampleInstanceProxy.floatResource.cancelObservation();
            }
        });

        waitSeconds(2);
        exampleInstance.floatResource.setValue(new FloatResourceValue(5.0f));

        // Check if you got notify
        waitSeconds(10);
        assertThat(gotNotifications, equalTo(1));
        assertThat(exampleInstanceProxy.floatResource.getValue().value, greaterThan(2.0f));

    }

}
