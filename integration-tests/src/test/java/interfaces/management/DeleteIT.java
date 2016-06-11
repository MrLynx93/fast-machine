package interfaces.management;

import com.agh.fastmachine.client.api.Client;
import com.agh.fastmachine.client.api.model.ObjectBase;
import com.agh.fastmachine.server.api.ClientProxy;
import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.exception.ObjectDeletedException;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectTree;
import com.agh.fastmachine.server.api.model.builtin.SecurityObjectInstanceProxy;
import org.junit.Test;
import util.model.ExampleObjectInstanceProxy;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import static util.TestUtil.startExampleClient;
import static util.TestUtil.startExampleServer;

public class DeleteIT {
    private static final String ENDPOINT_CLIENT_NAME = "132:123:123";
    private ObjectBaseProxy<ExampleObjectInstanceProxy> exampleObjectProxy;
    private ExampleObjectInstanceProxy exampleInstanceProxy;
    private ObjectBase<?> exampleObjectOnClient;

    @Test
    public void shouldDeleteSecurityObjectInstance() throws Exception {
        Server server = startExampleServer();
        Client client = startExampleClient(ENDPOINT_CLIENT_NAME);
        ClientProxy clientProxy = server.getClientForEndpointName(ENDPOINT_CLIENT_NAME);

        ObjectTree objectTree = clientProxy.getObjectTree();
        ObjectBaseProxy<SecurityObjectInstanceProxy> security = objectTree.getObjectForType(SecurityObjectInstanceProxy.class);
        SecurityObjectInstanceProxy securityInstance = security.getInstance(0);
        ObjectBase<?> securityOnClient = client.getObjectBaseMap().get(security.getId());

        securityInstance.delete();

        assertThat(securityInstance.isDeleted(), equalTo(true));
        assertNull(security.getInstance(0));
        assertNull(securityOnClient.getObjectInstances().get(0));

    }

    @Test
    public void shouldDeleteObjectInstance() throws Exception {
        Server server = startExampleServer();
        Client client = startExampleClient(ENDPOINT_CLIENT_NAME);
        ClientProxy clientProxy = server.getClientForEndpointName(ENDPOINT_CLIENT_NAME);

        ObjectTree objectTree = clientProxy.getObjectTree();
        exampleObjectProxy = objectTree.getObjectForType(ExampleObjectInstanceProxy.class);
        exampleInstanceProxy = exampleObjectProxy.getInstance(1);
        exampleObjectOnClient = client.getObjectBaseMap().get(exampleObjectProxy.getId());

        assertThat(exampleInstanceProxy.isDeleted(), equalTo(false));
        assertThat(exampleInstanceProxy.dateResource.isDeleted(), equalTo(false));
        assertThat(exampleInstanceProxy.stringResource.isDeleted(), equalTo(false));
        assertThat(exampleInstanceProxy.floatResource.isDeleted(), equalTo(false));
        assertThat(exampleInstanceProxy.integerResource.isDeleted(), equalTo(false));
        assertNotNull(exampleObjectOnClient.getObjectInstances().get(1));

        exampleInstanceProxy.delete();

        assertThat(exampleInstanceProxy.isDeleted(), equalTo(true));
        assertThat(exampleInstanceProxy.dateResource.isDeleted(), equalTo(true));
        assertThat(exampleInstanceProxy.stringResource.isDeleted(), equalTo(true));
        assertThat(exampleInstanceProxy.floatResource.isDeleted(), equalTo(true));
        assertThat(exampleInstanceProxy.integerResource.isDeleted(), equalTo(true));
        assertNull(exampleObjectProxy.getInstance(1));
        assertNull(exampleObjectOnClient.getObjectInstances().get(1));
    }

    @Test
    public void shouldNotBeAbleToUseDeletedObjectInstance() throws Exception {
        Server server = startExampleServer();
        Client client = startExampleClient(ENDPOINT_CLIENT_NAME);
        ClientProxy clientProxy = server.getClientForEndpointName(ENDPOINT_CLIENT_NAME);

        ObjectTree objectTree = clientProxy.getObjectTree();
        exampleObjectProxy = objectTree.getObjectForType(ExampleObjectInstanceProxy.class);
        exampleInstanceProxy = exampleObjectProxy.getInstance(1);
        exampleObjectOnClient = client.getObjectBaseMap().get(exampleObjectProxy.getId());

        assertThat(exampleInstanceProxy.isDeleted(), equalTo(false));
        assertThat(exampleInstanceProxy.dateResource.isDeleted(), equalTo(false));
        assertThat(exampleInstanceProxy.stringResource.isDeleted(), equalTo(false));
        assertThat(exampleInstanceProxy.floatResource.isDeleted(), equalTo(false));
        assertThat(exampleInstanceProxy.integerResource.isDeleted(), equalTo(false));
        assertNotNull(exampleObjectOnClient.getObjectInstances().get(1));

        try {
            exampleInstanceProxy.delete();
            exampleInstanceProxy.read();
            fail();
        } catch (ObjectDeletedException e) {

        }
    }

}
