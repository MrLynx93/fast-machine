package interfaces.management;

import com.agh.fastmachine.client.api.Client;
import com.agh.fastmachine.core.api.model.resourcevalue.IntegerResourceValue;
import com.agh.fastmachine.server.api.ClientProxy;
import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectTree;
import org.junit.Test;
import util.model.ExampleObjectInstanceProxy;

import java.util.Date;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static util.TestUtil.startExampleClient;
import static util.TestUtil.startExampleServer;

public class ReadIT { // TODO test multiple
    private static final String ENDPOINT_CLIENT_NAME = "132:123:123";
    private ObjectBaseProxy<ExampleObjectInstanceProxy> exampleObjectProxy;

    @Test
    public void shouldReadObjectValue() throws Exception {
        // Create Server and Client with Factory bootstrap
        // Register is done inside starting Client
        Server server = startExampleServer();
        Client client = startExampleClient(ENDPOINT_CLIENT_NAME);
        ClientProxy clientProxy = server.getClientForEndpointName(ENDPOINT_CLIENT_NAME);

        ObjectTree objectTree = clientProxy.getObjectTree();
        exampleObjectProxy = objectTree.getObjectForType(ExampleObjectInstanceProxy.class);
        ExampleObjectInstanceProxy exampleInstanceProxy1 = exampleObjectProxy.getInstance(1);
        ExampleObjectInstanceProxy exampleInstanceProxy2 = exampleObjectProxy.getInstance(4);

        // Check if values are empty
        assertThat(exampleInstanceProxy1.floatResource.getValue().value, nullValue());
        assertThat(exampleInstanceProxy2.stringResource.getValue().value, nullValue());

        assertThat(exampleInstanceProxy1.floatResource.isChanged(), equalTo(true));
        assertThat(exampleInstanceProxy2.stringResource.isChanged(), equalTo(true));
        assertThat(exampleInstanceProxy1.isChanged(), equalTo(true));
        assertThat(exampleInstanceProxy2.isChanged(), equalTo(true));

        assertThat(exampleObjectProxy.isChanged(), equalTo(true));

        // Read object
        exampleObjectProxy.read();

        // Check if values were read
        assertThat(exampleObjectProxy.isChanged(), equalTo(false));

        assertThat(exampleInstanceProxy1.floatResource.getValue().value, equalTo(1.1f));
        assertThat(exampleInstanceProxy1.floatResource.isChanged(), equalTo(false));
        assertThat(exampleInstanceProxy1.isChanged(), equalTo(false));

        assertThat(exampleInstanceProxy2.stringResource.getValue().value, equalTo("Hello, instance 4 :)"));
        assertThat(exampleInstanceProxy2.stringResource.isChanged(), equalTo(false));
        assertThat(exampleInstanceProxy2.isChanged(), equalTo(false));

//        Thread.sleep(1000000000000000000L);
    }

    @Test
    public void shouldReadObjectInstanceValue() throws Exception {
        // Create Server and Client with Factory bootstrap
        // Register is done inside starting Client
        Server server = startExampleServer();
        Client client = startExampleClient(ENDPOINT_CLIENT_NAME);
        ClientProxy clientProxy = server.getClientForEndpointName(ENDPOINT_CLIENT_NAME);

        ObjectTree objectTree = clientProxy.getObjectTree();
        exampleObjectProxy = objectTree.getObjectForType(ExampleObjectInstanceProxy.class);
        ExampleObjectInstanceProxy exampleInstanceProxy1 = exampleObjectProxy.getInstance(1);
        ExampleObjectInstanceProxy exampleInstanceProxy2 = exampleObjectProxy.getInstance(4);

        // Check if values are empty
        assertThat(exampleInstanceProxy1.floatResource.isChanged(), equalTo(true));
        assertThat(exampleInstanceProxy1.floatResource.getValue().value, nullValue());
        assertThat(exampleInstanceProxy1.dateResource.isChanged(), equalTo(true));
        assertThat(exampleInstanceProxy1.dateResource.getValue().value, nullValue());
        assertThat(exampleInstanceProxy2.stringResource.isChanged(), equalTo(true));
        assertThat(exampleInstanceProxy2.stringResource.getValue().value, nullValue());
        assertThat(exampleInstanceProxy1.isChanged(), equalTo(true));
        assertThat(exampleInstanceProxy2.isChanged(), equalTo(true));

        // Read objects
        exampleInstanceProxy1.read();
        exampleInstanceProxy2.read();

        // Check if values were read
        assertThat(exampleInstanceProxy1.floatResource.getValue().value, equalTo(1.1f));
        assertThat(exampleInstanceProxy1.floatResource.isChanged(), equalTo(false));
        assertThat(exampleInstanceProxy1.isChanged(), equalTo(false));

        assertThat(exampleInstanceProxy1.dateResource.getValue().value, equalTo(new Date(10, 8, 26)));
        assertThat(exampleInstanceProxy1.dateResource.isChanged(), equalTo(false));
        assertThat(exampleInstanceProxy1.isChanged(), equalTo(false));

        assertThat(exampleInstanceProxy2.stringResource.getValue().value, equalTo("Hello, instance 4 :)"));
        assertThat(exampleInstanceProxy2.stringResource.isChanged(), equalTo(false));
        assertThat(exampleInstanceProxy2.isChanged(), equalTo(false));
    }

    @Test
    public void shouldReadObjectResourceValue() throws Exception {
        // Create Server and Client with Factory bootstrap
        // Register is done inside starting Client
        Server server = startExampleServer();
        Client client = startExampleClient(ENDPOINT_CLIENT_NAME);
        ClientProxy clientProxy = server.getClientForEndpointName(ENDPOINT_CLIENT_NAME);

        ObjectTree objectTree = clientProxy.getObjectTree();
        exampleObjectProxy = objectTree.getObjectForType(ExampleObjectInstanceProxy.class);
        ExampleObjectInstanceProxy exampleInstanceProxy1 = exampleObjectProxy.getInstance(1);
        ExampleObjectInstanceProxy exampleInstanceProxy2 = exampleObjectProxy.getInstance(4);

        // Check if values are empty
        assertThat(exampleInstanceProxy1.floatResource.isChanged(), equalTo(true));
        assertThat(exampleInstanceProxy1.floatResource.getValue().value, nullValue());
        assertThat(exampleInstanceProxy2.stringResource.isChanged(), equalTo(true));
        assertThat(exampleInstanceProxy2.stringResource.getValue().value, nullValue());

        // Read objects
        exampleInstanceProxy1.floatResource.read();
        exampleInstanceProxy2.stringResource.read();

        // Check if values were read
        assertThat(exampleInstanceProxy1.floatResource.getValue().value, equalTo(1.1f));
        assertThat(exampleInstanceProxy1.floatResource.isChanged(), equalTo(false));
        assertThat(exampleInstanceProxy1.isChanged(), equalTo(true));

        assertThat(exampleInstanceProxy2.stringResource.getValue().value, equalTo("Hello, instance 4 :)"));
        assertThat(exampleInstanceProxy2.stringResource.isChanged(), equalTo(false));
        assertThat(exampleInstanceProxy2.isChanged(), equalTo(true));
    }

    @Test
    public void shouldReadMultipleResource() throws Exception {
        // Create Server and Client with Factory bootstrap
        // Register is done inside starting Client
        Server server = startExampleServer();
        Client client = startExampleClient(ENDPOINT_CLIENT_NAME);
        ClientProxy clientProxy = server.getClientForEndpointName(ENDPOINT_CLIENT_NAME);

        ObjectTree objectTree = clientProxy.getObjectTree();
        exampleObjectProxy = objectTree.getObjectForType(ExampleObjectInstanceProxy.class);
        ExampleObjectInstanceProxy exampleInstanceProxy1 = exampleObjectProxy.getInstance(1);

        // Check if values are empty
        assertThat(exampleInstanceProxy1.multipleIntegerResource.isChanged(), equalTo(true));
        assertThat(exampleInstanceProxy1.multipleIntegerResource.getValue(), nullValue());

        // Read objects
        exampleInstanceProxy1.multipleIntegerResource.setValue(new IntegerResourceValue(55, 1));
        exampleInstanceProxy1.multipleIntegerResource.setValue(new IntegerResourceValue(66, 2));
        exampleInstanceProxy1.multipleIntegerResource.write();

        exampleInstanceProxy1.multipleIntegerResource.read();

        // Check if values were read
        assertThat(exampleInstanceProxy1.multipleIntegerResource.getValue(1).value, equalTo(55));
        assertThat(exampleInstanceProxy1.multipleIntegerResource.getValue(2).value, equalTo(66));
        assertThat(exampleInstanceProxy1.multipleIntegerResource.isChanged(), equalTo(false));
    }
}
