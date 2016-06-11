package interfaces;


import com.agh.fastmachine.client.api.Client;
import com.agh.fastmachine.client.api.model.ObjectBase;
import com.agh.fastmachine.client.api.model.builtin.AccessControlObjectBase;
import com.agh.fastmachine.client.api.model.builtin.SecurityObjectBase;
import com.agh.fastmachine.client.api.model.builtin.ServerObjectBase;
import com.agh.fastmachine.server.api.ClientProxy;
import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.listener.RegistrationListener;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectTree;
import com.agh.fastmachine.server.internal.client.ClientProxyStatus;
import org.junit.Test;
import util.model.ExampleObjectBase;
import util.model.ExampleObjectInstanceProxy;

import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static util.TestUtil.*;

public class RegisterIT {
    private static final String ENDPOINT_CLIENT_NAME = "EndpointClientName:4123:143";
    public static final int EXAMPLE_OBJECT_ID = 1789;

    private ObjectBaseProxy<ExampleObjectInstanceProxy> exampleObjectProxy;
    private SecurityObjectBase securityObject;
    private ServerObjectBase serverObject;
    private AccessControlObjectBase acoObject;
    private ExampleObjectBase exampleObject;
    private boolean updated = false;

    @Test
    public void shouldRegister() throws Exception {
        // Create Server and Client with Factory bootstrap
        // Register is done inside starting Client
        Server server = startExampleServer();
        Client client = startExampleClient(ENDPOINT_CLIENT_NAME);

        // Collect init data
        ClientProxy clientProxy = server.getClientForEndpointName(ENDPOINT_CLIENT_NAME);
        ObjectTree objectTree = clientProxy.getObjectTree();
        exampleObjectProxy = objectTree.getObjectForType(ExampleObjectInstanceProxy.class);

        Map<Integer, ObjectBase<?>> objects = client.getObjectBaseMap();
        securityObject = (SecurityObjectBase) objects.get(0);
        serverObject = (ServerObjectBase) objects.get(1);
        acoObject = (AccessControlObjectBase) objects.get(2);
        exampleObject = (ExampleObjectBase) objects.get(EXAMPLE_OBJECT_ID);

        ExampleObjectInstanceProxy exampleInstanceProxy1 = exampleObjectProxy.getInstance(1);
        ExampleObjectInstanceProxy exampleInstanceProxy2 = exampleObjectProxy.getInstance(4);

        // Assert expected init data
        assertThat(clientProxy.getStatus(), equalTo(ClientProxyStatus.REGISTERED));

        assertThat(securityObject.getObjectInstance(0), notNullValue());
        assertThat(serverObject.getObjectInstance(0), notNullValue());
        assertThat(acoObject.getObjectInstance(0), notNullValue());
        assertThat(exampleObject.getObjectInstance(1), notNullValue());
        assertThat(exampleObject.getObjectInstance(4), notNullValue());

        // Assert that objects on Server are NOT initialized(but present) - you have to read them
        assertThat(exampleInstanceProxy1.floatResource.getValue().value, nullValue());
        assertThat(exampleInstanceProxy2.stringResource.getValue().value, nullValue());
    }

    @Test
    public void shouldDeregister() throws Exception {
        // Create Server and Client with Factory bootstrap
        // Register is done inside starting Client
        Server server = startExampleServer();
        Client client = startExampleClient(ENDPOINT_CLIENT_NAME);
        ClientProxy clientProxy = server.getClientForEndpointName(ENDPOINT_CLIENT_NAME);

        assertThat(clientProxy.getStatus(), equalTo(ClientProxyStatus.REGISTERED));

        Map<Integer, ObjectBase<?>> objects = client.getObjectBaseMap();
        serverObject = (ServerObjectBase) objects.get(1);

        client.deregister(0);
        assertThat(clientProxy.getStatus(), equalTo(ClientProxyStatus.BOOTSTRAPPED));
    }

    @Test
    public void shouldSendUpdates() throws Exception {
        // Create Server and Client with Factory bootstrap
        // Register is done inside starting Client
        Server server = startExampleServer();
        server.setRegistrationListener(new RegistrationListener() {
            @Override
            public void onUpdate(ClientProxy client) {
                updated = true;
            }
        });
        Client client = startExampleClient(ENDPOINT_CLIENT_NAME);
        ClientProxy clientProxy = server.getClientForEndpointName(ENDPOINT_CLIENT_NAME);

        assertThat(clientProxy.getStatus(), equalTo(ClientProxyStatus.REGISTERED));

        waitSeconds(10);
        assertThat(updated, equalTo(true));
    }

}
