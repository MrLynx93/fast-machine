package com.agh.fastmachine.server.internal.client;

import com.agh.fastmachine.server.api.ClientProxy;
import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.listener.RegistrationListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ClientManager {
    private static final Logger LOG = LoggerFactory.getLogger(ClientManager.class);

    private Map<String, ClientProxyImpl> clientsByEndpointName = new HashMap<>();
    private final Server server;
    private RegistrationListener registrationListener;

    public ClientManager(Server server) {
        this.server = server;
    }

    public synchronized ClientProxyImpl createClient(String endpointClientName) {
        if (clientsByEndpointName.containsKey(endpointClientName)) {
            LOG.debug("Client {} already exists. Can't create proxy again", endpointClientName);
            throw new IllegalStateException("Client already exists");
        }
        ClientProxyImpl clientProxy = new ClientProxyImpl(server, endpointClientName);
        clientProxy.setRegistrationListener(registrationListener);

        clientsByEndpointName.put(endpointClientName, clientProxy);
        return clientProxy;
    }

    public ClientProxyImpl getClientForEndpointName(String clientEndpointName) {
        return clientsByEndpointName.get(clientEndpointName);
    }

    public void removeClientForEndpointName(String endpointClientName) {
        clientsByEndpointName.remove(endpointClientName);
    }

    public Map<String, ? extends ClientProxy> getClients() {
        return clientsByEndpointName;
    }

    public Server getServer() {
        return server;
    }

    public void setRegistrationListener(RegistrationListener registrationListener) {
        this.registrationListener = registrationListener;
    }

}
