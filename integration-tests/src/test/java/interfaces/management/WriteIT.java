package interfaces.management;

import com.agh.fastmachine.client.api.Client;
import com.agh.fastmachine.core.api.model.resourcevalue.FloatResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.IntegerResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.StringResourceValue;
import com.agh.fastmachine.server.api.ClientProxy;
import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectTree;
import com.agh.fastmachine.server.internal.client.ClientProxyStatus;
import com.agh.fastmachine.server.internal.transport.stats.TimeoutException;
import org.junit.Test;
import util.model.ExampleObjectInstanceProxy;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static util.TestUtil.startExampleClient;
import static util.TestUtil.startExampleServer;

public class WriteIT { // TODO multiple resource
    private static final String ENDPOINT_CLIENT_NAME = "132:123:123";
    private ObjectBaseProxy<ExampleObjectInstanceProxy> exampleObjectProxy;

    @Test
    public void shouldWriteObjectInstanceValue() throws Exception {
        // Create Server and Client with Factory bootstrap
        // Register is done inside starting Client
        Server server = startExampleServer();
        Client client = startExampleClient(ENDPOINT_CLIENT_NAME);
        ClientProxy clientProxy = server.getClientForEndpointName(ENDPOINT_CLIENT_NAME);

        assertThat(clientProxy.getStatus(), equalTo(ClientProxyStatus.REGISTERED));

        ObjectTree objectTree = clientProxy.getObjectTree();
        exampleObjectProxy = objectTree.getObjectForType(ExampleObjectInstanceProxy.class);
        ExampleObjectInstanceProxy exampleInstanceProxy1 = exampleObjectProxy.getInstance(1);
        ExampleObjectInstanceProxy exampleInstanceProxy2 = exampleObjectProxy.getInstance(4);

        // Read objects
        exampleInstanceProxy1.read();

        // Check if values were read
        assertThat(exampleInstanceProxy1.floatResource.getValue().value, equalTo(1.1f));
        assertThat(exampleInstanceProxy1.stringResource.getValue().value, equalTo("Hello, instance 1 :)"));
        assertThat(exampleInstanceProxy1.integerResource.getValue().value, equalTo(4));

        assertThat(exampleInstanceProxy1.floatResource.isChanged(), equalTo(false));
        assertThat(exampleInstanceProxy1.stringResource.isChanged(), equalTo(false));
        assertThat(exampleInstanceProxy1.integerResource.isChanged(), equalTo(false));

        // Set new value
        exampleInstanceProxy1.floatResource.setValue(new FloatResourceValue(4.4f));
        exampleInstanceProxy1.stringResource.setValue(new StringResourceValue("New Value"));
        exampleInstanceProxy1.integerResource.setValue(new IntegerResourceValue(500));

        assertThat(exampleInstanceProxy1.floatResource.isChanged(), equalTo(true));
        assertThat(exampleInstanceProxy1.stringResource.isChanged(), equalTo(true));
        assertThat(exampleInstanceProxy1.integerResource.isChanged(), equalTo(true));

        exampleInstanceProxy1.write();

        // Check if new value is set
        assertThat(exampleInstanceProxy1.floatResource.getValue().value, equalTo(4.4f));
        assertThat(exampleInstanceProxy1.stringResource.getValue().value, equalTo("New Value"));
        assertThat(exampleInstanceProxy1.integerResource.getValue().value, equalTo(500));

        assertThat(exampleInstanceProxy1.floatResource.isChanged(), equalTo(false));
        assertThat(exampleInstanceProxy1.stringResource.isChanged(), equalTo(false));
        assertThat(exampleInstanceProxy1.integerResource.isChanged(), equalTo(false));

        exampleInstanceProxy1.read();

        assertThat(exampleInstanceProxy1.floatResource.getValue().value, equalTo(4.4f));
        assertThat(exampleInstanceProxy1.stringResource.getValue().value, equalTo("New Value"));
        assertThat(exampleInstanceProxy1.integerResource.getValue().value, equalTo(500));

        assertThat(exampleInstanceProxy1.floatResource.isChanged(), equalTo(false));
        assertThat(exampleInstanceProxy1.stringResource.isChanged(), equalTo(false));
        assertThat(exampleInstanceProxy1.integerResource.isChanged(), equalTo(false));
    }

    @Test
    public void shouldWriteObjectResourceValue() throws Exception {
        // Create Server and Client with Factory bootstrap
        // Register is done inside starting Client
        Server server = startExampleServer();
        Client client = startExampleClient(ENDPOINT_CLIENT_NAME);
        ClientProxy clientProxy = server.getClientForEndpointName(ENDPOINT_CLIENT_NAME);

        assertThat(clientProxy.getStatus(), equalTo(ClientProxyStatus.REGISTERED));

        ObjectTree objectTree = clientProxy.getObjectTree();
        exampleObjectProxy = objectTree.getObjectForType(ExampleObjectInstanceProxy.class);
        ExampleObjectInstanceProxy exampleInstanceProxy1 = exampleObjectProxy.getInstance(1);
        ExampleObjectInstanceProxy exampleInstanceProxy2 = exampleObjectProxy.getInstance(4);

        // Read objects
        exampleInstanceProxy1.floatResource.read();

        // Check if values were read
        assertThat(exampleInstanceProxy1.floatResource.getValue().value, equalTo(1.1f));
        assertThat(exampleInstanceProxy1.floatResource.isChanged(), equalTo(false));

        // Set new value
        exampleInstanceProxy1.floatResource.setValue(new FloatResourceValue(4.4f));
        assertThat(exampleInstanceProxy1.floatResource.isChanged(), equalTo(true));
        exampleInstanceProxy1.stringResource.setValue(new StringResourceValue("asdsad"));
        exampleInstanceProxy1.stringResource.write();
        exampleInstanceProxy1.floatResource.write();

        // Check if new value is set
        assertThat(exampleInstanceProxy1.floatResource.getValue().value, equalTo(4.4f));
        assertThat(exampleInstanceProxy1.floatResource.isChanged(), equalTo(false));
        exampleInstanceProxy1.floatResource.read();
        assertThat(exampleInstanceProxy1.floatResource.getValue().value, equalTo(4.4f));
        assertThat(exampleInstanceProxy1.floatResource.isChanged(), equalTo(false));
        exampleInstanceProxy1.stringResource.read();
        assertThat(exampleInstanceProxy1.stringResource.getValue().value, equalTo("asdsad"));
    }

    @Test
    public void shouldWriteObjectMultipleResourceValue() {
        // Create Server and Client with Factory bootstrap
        // Register is done inside starting Client
        Server server = startExampleServer();
        Client client = startExampleClient(ENDPOINT_CLIENT_NAME);
        ClientProxy clientProxy = server.getClientForEndpointName(ENDPOINT_CLIENT_NAME);

        assertThat(clientProxy.getStatus(), equalTo(ClientProxyStatus.REGISTERED));

        ObjectTree objectTree = clientProxy.getObjectTree();
        exampleObjectProxy = objectTree.getObjectForType(ExampleObjectInstanceProxy.class);
        ExampleObjectInstanceProxy exampleInstanceProxy = exampleObjectProxy.getInstance(1);

        // Read objects
        try {
            exampleInstanceProxy.multipleIntegerResource.read();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        // Check if values were read
        assertThat(exampleInstanceProxy.multipleIntegerResource.getValues().get(0).value, equalTo(5555));
        assertThat(exampleInstanceProxy.multipleIntegerResource.getValues().get(1).value, equalTo(5555));
        assertThat(exampleInstanceProxy.multipleIntegerResource.isChanged(), equalTo(false));

        // Set new value
        exampleInstanceProxy.multipleIntegerResource.setValue(new IntegerResourceValue(6666, 0));
        exampleInstanceProxy.multipleIntegerResource.setValue(new IntegerResourceValue(7777, 1));
        assertThat(exampleInstanceProxy.multipleIntegerResource.isChanged(), equalTo(true));
        exampleInstanceProxy.multipleIntegerResource.write();

        //Check if new value was set
        exampleInstanceProxy.read();
        assertThat(exampleInstanceProxy.multipleIntegerResource.getValues().get(0).value, equalTo(6666));
        assertThat(exampleInstanceProxy.multipleIntegerResource.getValues().get(1).value, equalTo(7777));

    }



}
