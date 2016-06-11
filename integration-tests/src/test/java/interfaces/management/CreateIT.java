package interfaces.management;

import com.agh.fastmachine.client.api.Client;
import com.agh.fastmachine.core.api.model.resourcevalue.FloatResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.StringResourceValue;
import com.agh.fastmachine.server.api.ClientProxy;
import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectTree;
import org.junit.Test;
import util.TestUtil;
import util.model.ExampleObjectInstanceProxy;

import static org.fest.assertions.Assertions.assertThat;

// TODO now create works differently
public class CreateIT {
    private static final String ENDPOINT_CLIENT_NAME = "132:123:123";
    private ObjectBaseProxy<ExampleObjectInstanceProxy> exampleObjectProxy;

    @Test
    public void shouldCreateObjectInstanceWithKnownId() throws Exception {
//        Server server = TestUtil.startExampleServer();
//        Client client = TestUtil.startExampleClient(ENDPOINT_CLIENT_NAME);
//        ClientProxy clientProxy = server.getClientForEndpointName(ENDPOINT_CLIENT_NAME);
//
//        // Check if was not created
//        ObjectTree objectTree = clientProxy.getObjectTree();
//        exampleObjectProxy = objectTree.getObjectForType(ExampleObjectInstanceProxy.class);
//
//        assertThat(exampleObjectProxy.getInstance(4)).isNotNull();
//        assertThat(exampleObjectProxy.getInstance(5)).isNull();
//
//        // Create new instance with ID 100
//        ExampleObjectInstanceProxy newInstance = new ExampleObjectInstanceProxy();
//        newInstance.floatResource.setValue(new FloatResourceValue(2.3f));
//        newInstance.stringResource.setValue(new StringResourceValue("Hello, new instance!"));
////        newInstance = clientProxy.create(newInstance, 100);
//
//        assertThat(newInstance).isNotNull();
//        assertThat(newInstance.getId()).isEqualTo(100); // next ID after 4
//        assertThat(newInstance.isRemote()).isTrue();
//        assertThat(objectTree.getObjectForType(ExampleObjectInstanceProxy.class).getInstance(100)).isSameAs(newInstance);
    }

    @Test
    public void shouldCreateObjectInstanceWithUnknownId() throws Exception {
//        Server server = TestUtil.startExampleServer();
//        Client client = TestUtil.startExampleClient(ENDPOINT_CLIENT_NAME);
//        ClientProxy clientProxy = server.getClientForEndpointName(ENDPOINT_CLIENT_NAME);
//
//        // Check if was not created
//        ObjectTree objectTree = clientProxy.getObjectTree();
//        exampleObjectProxy = objectTree.getObjectForType(ExampleObjectInstanceProxy.class);
//
//        assertThat(exampleObjectProxy.getInstance(4)).isNotNull();
//        assertThat(exampleObjectProxy.getInstance(5)).isNull();
//
//        // Create new instance with unknown ID
//        ExampleObjectInstanceProxy newInstance = new ExampleObjectInstanceProxy();
//        newInstance.floatResource.setValue(new FloatResourceValue(2.3f));
//        newInstance.stringResource.setValue(new StringResourceValue("Hello, new instance!"));
//        newInstance = clientProxy.create(newInstance);
//
//        assertThat(newInstance).isNotNull();
//        assertThat(newInstance.getId()).isEqualTo(5); // next ID after 4
//        assertThat(newInstance.isRemote()).isTrue();
//        assertThat(objectTree.getObjectForType(ExampleObjectInstanceProxy.class).getInstance(5)).isSameAs(newInstance);
    }

}
