package interfaces.observe;

import com.agh.fastmachine.client.api.Client;
import com.agh.fastmachine.core.api.model.Attributes;
import com.agh.fastmachine.core.api.model.resourcevalue.FloatResourceValue;
import com.agh.fastmachine.server.api.ClientProxy;
import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.listener.ObservationListener;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;
import com.agh.fastmachine.server.api.model.ObjectTree;
import org.hamcrest.Matchers;
import org.junit.Test;
import util.model.ExampleObjectInstance;
import util.model.ExampleObjectInstanceProxy;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static util.TestUtil.*;

public class ObserveNumericIT {
    private static final String ENDPOINT_CLIENT_NAME = "132:123:123";
    public static final int EXAMPLE_OBJECT_ID = 1789;
    private ObjectBaseProxy<ExampleObjectInstanceProxy> exampleObjectProxy;
    private ExampleObjectInstanceProxy exampleInstanceProxy;
    private ExampleObjectInstance exampleInstance;
    private int gotNotifications = 0;

    @Test
    public void shouldObserveResourceWithStepAttribute() throws Exception {
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
        exampleInstanceProxy.floatResource.read();
        assertThat(exampleInstanceProxy.floatResource.getValue().value, equalTo(1.1f));

        // Set write attributes
        final Attributes attributes = exampleInstanceProxy.floatResource.getAttributes();
        attributes.maximumPeriod = 100;
        attributes.minimumPeriod = 2;
        attributes.step = 5.0;
        exampleInstanceProxy.floatResource.writeAttributes();

        // Start observing
        final long startTime = System.currentTimeMillis();
        exampleInstanceProxy.floatResource.observe(new ObservationListener<ObjectResourceProxy<FloatResourceValue>>() {
            public long firstNotifyTime = 0;

            @Override
            public void onNotify(ObjectResourceProxy<FloatResourceValue> node) {
                gotNotifications++;

                if (firstNotifyTime > 0) {
                    assertThat(System.currentTimeMillis() - firstNotifyTime, greaterThan(attributes.minimumPeriod * 1000L));
                }
                firstNotifyTime = System.currentTimeMillis();
            }
        });

        // Should not notify when step was lesser than 5.0f
        exampleInstance.floatResource.setValue(new FloatResourceValue(3.0f));
        waitSeconds(3);
        assertThat(gotNotifications, Matchers.equalTo(0));

        // Should notify when step was greater than 5.0f
        exampleInstance.floatResource.setValue(new FloatResourceValue((float) (3.0f + attributes.step + 0.1f)));

        // Check if you got notify
        waitSeconds(attributes.minimumPeriod + 1);
        assertThat(gotNotifications, Matchers.equalTo(1));
    }

    @Test
    public void shouldObserveResourceWithLesserThanAttribute() throws Exception {
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
        exampleInstanceProxy.floatResource.read();
        assertThat(exampleInstanceProxy.floatResource.getValue().value, equalTo(1.1f));

        // Set write attributes
        final Attributes writeAttributes = exampleInstanceProxy.floatResource.getAttributes();
        writeAttributes.maximumPeriod = 100;
        writeAttributes.minimumPeriod = 2;
        writeAttributes.lessThan = 1.0;
        exampleInstanceProxy.floatResource.writeAttributes();

        // Start observing
        final long startTime = System.currentTimeMillis();
        exampleInstanceProxy.floatResource.observe(new ObservationListener<ObjectResourceProxy<FloatResourceValue>>() {
            public long firstNotifyTime = 0;

            @Override
            public void onNotify(ObjectResourceProxy<FloatResourceValue> node) {
                gotNotifications++;
                assertThat(((double) node.getValue().value), lessThan(writeAttributes.lessThan));

                if (firstNotifyTime > 0) {
                    assertThat(System.currentTimeMillis() - firstNotifyTime, greaterThan(writeAttributes.minimumPeriod * 1000L));
                }
                firstNotifyTime = System.currentTimeMillis();
            }
        });

        // Should not notify when not lesser than
        exampleInstance.floatResource.setValue(new FloatResourceValue(3.0f));
        waitSeconds(3);
        assertThat(gotNotifications, Matchers.equalTo(0));

        // Should notify when lesser than
        exampleInstance.floatResource.setValue(new FloatResourceValue(0.5f));

        // Check if you got notify
        waitSeconds(writeAttributes.minimumPeriod + 1);
        assertThat(gotNotifications, Matchers.equalTo(1));
        assertThat((double)exampleInstanceProxy.floatResource.getValue().value, lessThan(writeAttributes.lessThan));

    }

    @Test
    public void shouldObserveResourceWithGreaterThanAttribute() throws Exception {
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
        exampleInstanceProxy.floatResource.read();
        assertThat(exampleInstanceProxy.floatResource.getValue().value, equalTo(1.1f));

        // Set write attributes
        final Attributes writeAttributes = exampleInstanceProxy.floatResource.getAttributes();
        writeAttributes.maximumPeriod = 100;
        writeAttributes.minimumPeriod = 2;
        writeAttributes.greaterThan = 4.0;
        exampleInstanceProxy.floatResource.writeAttributes();

        // Start observing
        final long startTime = System.currentTimeMillis();
        exampleInstanceProxy.floatResource.observe(new ObservationListener<ObjectResourceProxy<FloatResourceValue>>() {
            public long firstNotifyTime = 0;

            @Override
            public void onNotify(ObjectResourceProxy<FloatResourceValue> node) {
                gotNotifications++;
                assertThat(node.getValue().value, greaterThan(4.0f));

                if (firstNotifyTime > 0) {
                    assertThat(System.currentTimeMillis() - firstNotifyTime, greaterThan(writeAttributes.minimumPeriod * 1000L));
                }
                firstNotifyTime = System.currentTimeMillis();
            }
        });

        // Should not notify when not greater than
        exampleInstance.floatResource.setValue(new FloatResourceValue(3.0f));
        waitSeconds(3);
        assertThat(gotNotifications, Matchers.equalTo(0));

        // Should notify when greater than
        exampleInstance.floatResource.setValue(new FloatResourceValue(5.0f));

        // Check if you got notify
        waitSeconds(writeAttributes.minimumPeriod + 1);
        assertThat(gotNotifications, Matchers.equalTo(1));
        assertThat((double)exampleInstanceProxy.floatResource.getValue().value, greaterThan(writeAttributes.greaterThan));
    }

    @Test
    public void shouldObserveResourceWithNumericAttributesNull() throws Exception {
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
        exampleInstanceProxy.floatResource.read();
        assertThat(exampleInstanceProxy.floatResource.getValue().value, equalTo(1.1f));

        // Set write attributes
        final Attributes attributes = exampleInstanceProxy.floatResource.getAttributes();
        attributes.maximumPeriod = 100;
        attributes.minimumPeriod = 2;
        exampleInstanceProxy.floatResource.writeAttributes();

        // Start observing
        exampleInstanceProxy.floatResource.observe(new ObservationListener<ObjectResourceProxy<FloatResourceValue>>() {
            private long firstNotifyTime = 0;

            @Override
            public void onNotify(ObjectResourceProxy<FloatResourceValue> node) {
                gotNotifications++;
                assertThat(node.getValue().value, greaterThan(4.0f));

                if (firstNotifyTime > 0) {
                    assertThat(System.currentTimeMillis() - firstNotifyTime, greaterThan(attributes.minimumPeriod * 1000L));
                }
                firstNotifyTime = System.currentTimeMillis();
            }
        });

        exampleInstance.floatResource.setValue(new FloatResourceValue(5.0f));
        assertThat(gotNotifications, Matchers.equalTo(0));

        // Check if you got notify
        waitSeconds(attributes.maximumPeriod);
        assertThat(gotNotifications, Matchers.equalTo(1));
        assertThat(exampleInstanceProxy.floatResource.getValue().value, greaterThan(2.0f));
    }

}
