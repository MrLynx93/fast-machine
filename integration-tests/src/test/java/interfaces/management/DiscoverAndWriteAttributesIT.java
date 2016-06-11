package interfaces.management;

import com.agh.fastmachine.client.api.Client;
import com.agh.fastmachine.server.api.ClientProxy;
import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectTree;
import org.junit.Test;
import util.model.ExampleObjectInstanceProxy;

import static org.fest.assertions.Assertions.assertThat;
import static util.TestUtil.startExampleClient;
import static util.TestUtil.startExampleServer;

public class DiscoverAndWriteAttributesIT {
    private static final String ENDPOINT_CLIENT_NAME = "132:123:123";
    private ObjectBaseProxy<ExampleObjectInstanceProxy> objectProxy;
    private ExampleObjectInstanceProxy instanceProxy;

    @Test
    public void shouldDiscoverObjectWriteAttributes() throws Exception {
        Server server = startExampleServer();
        Client client = startExampleClient(ENDPOINT_CLIENT_NAME);
        ClientProxy clientProxy = server.getClientForEndpointName(ENDPOINT_CLIENT_NAME);

        ObjectTree objectTree = clientProxy.getObjectTree();
        objectProxy = objectTree.getObjectForType(ExampleObjectInstanceProxy.class);
        instanceProxy = objectProxy.getInstance(1);

        objectProxy.getAttributes().maximumPeriod = 43;
        objectProxy.getAttributes().minimumPeriod = 42;
        objectProxy.writeAttributes();

        objectProxy.getAttributes().maximumPeriod = 0;
        objectProxy.getAttributes().minimumPeriod = 0;
        objectProxy.discover();

        assertThat(objectProxy.getAttributes().maximumPeriod).isEqualTo(43);
        assertThat(objectProxy.getAttributes().minimumPeriod).isEqualTo(42);
    }

    @Test
    public void shouldDiscoverObjectInstanceWriteAttributes() throws Exception {
        Server server = startExampleServer();
        Client client = startExampleClient(ENDPOINT_CLIENT_NAME);
        ClientProxy clientProxy = server.getClientForEndpointName(ENDPOINT_CLIENT_NAME);

        ObjectTree objectTree = clientProxy.getObjectTree();
        objectProxy = objectTree.getObjectForType(ExampleObjectInstanceProxy.class);
        instanceProxy = objectProxy.getInstance(1);

        instanceProxy.getAttributes().maximumPeriod = 43;
        instanceProxy.getAttributes().minimumPeriod = 42;
        instanceProxy.writeAttributes();

        instanceProxy.getAttributes().maximumPeriod = 0;
        instanceProxy.getAttributes().minimumPeriod = 0;
        instanceProxy.discover();

        assertThat(instanceProxy.getAttributes().maximumPeriod).isEqualTo(43);
        assertThat(instanceProxy.getAttributes().minimumPeriod).isEqualTo(42);
    }

    @Test
    public void shouldDiscoverObjectResourceWriteAttributes() throws Exception {
        Server server = startExampleServer();
        Client client = startExampleClient(ENDPOINT_CLIENT_NAME);
        ClientProxy clientProxy = server.getClientForEndpointName(ENDPOINT_CLIENT_NAME);

        ObjectTree objectTree = clientProxy.getObjectTree();
        objectProxy = objectTree.getObjectForType(ExampleObjectInstanceProxy.class);
        instanceProxy = objectProxy.getInstance(1);

        instanceProxy.floatResource.getAttributes().lessThan = 23.0;
        instanceProxy.floatResource.getAttributes().greaterThan = 22.0;
        instanceProxy.floatResource.getAttributes().maximumPeriod = 43;
        instanceProxy.floatResource.getAttributes().minimumPeriod = 42;
        instanceProxy.floatResource.writeAttributes();

        instanceProxy.floatResource.getAttributes().lessThan = 0.0;
        instanceProxy.floatResource.getAttributes().greaterThan = 0.0;
        instanceProxy.floatResource.getAttributes().maximumPeriod = 0;
        instanceProxy.floatResource.getAttributes().minimumPeriod = 0;
        instanceProxy.floatResource.discover();

        assertThat(instanceProxy.floatResource.getAttributes().maximumPeriod).isEqualTo(43);
        assertThat(instanceProxy.floatResource.getAttributes().minimumPeriod).isEqualTo(42);
        assertThat(instanceProxy.floatResource.getAttributes().lessThan).isEqualTo(23f);
        assertThat(instanceProxy.floatResource.getAttributes().greaterThan).isEqualTo(22f);
    }
}
