package com.agh.fastmachine.server.internal.client;

import com.agh.fastmachine.server.api.ClientProxy;
import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.internal.service.registrationinfo.RegistrationInfo;
import com.agh.fastmachine.server.internal.parser.RegistrationInfoParser;
import com.agh.fastmachine.server.api.model.ObjectTree;
import com.agh.fastmachine.server.internal.transport.TransportLayer;
import org.eclipse.californium.core.CoapServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientProxyImpl extends BaseRegistrationListener implements ClientProxy {
    private static final Logger LOG = LoggerFactory.getLogger(ClientProxyImpl.class);

    private TransportLayer transportLayer;
    private ObjectTree objectTree;
    private ClientProxyStatus status;
    private String endpointClientName;
    private String registrationEndpoint;
    private String clientUrl;
    private final Server server;
    private RegistrationInfo registrationInfo;

    public ClientProxyImpl(Server server, String endpointClientName) {
        this.server = server;
        this.endpointClientName = endpointClientName;
        this.status = ClientProxyStatus.CREATED;
        this.transportLayer = server.internal().getTransportLayer();
    }

    @Override
    public <T extends ObjectInstanceProxy> void create(T patternInstance) {
        transportLayer.createOperations(this).create(patternInstance);
    }

    @Override
    public <T extends ObjectInstanceProxy> void create(T patternInstance, int id) {
        transportLayer.createOperations(this).create(patternInstance, id);
    }

    @Override
    public String getClientEndpointName() {
        return endpointClientName;
    }

    @Override
    public ObjectTree getObjectTree() {
        return objectTree;
    }

    @Override
    public ClientProxyStatus getStatus() {
        return status;
    }

    public void setStatus(ClientProxyStatus status) {
        this.status = status;
    }

    @Override
    public boolean isRegistered() {
        return status == ClientProxyStatus.REGISTERED;
    }

    public String getRegistrationEndpoint() {
        return registrationEndpoint;
    }

    public String getClientUrl() {
        return clientUrl;
    }

    @Override
    protected void clearObjectTree() {
        this.objectTree = null;
    }

    public void setObjectTree(ObjectTree objectTree) {
        this.objectTree = objectTree;
    }

    public TransportLayer getTransportLayer() {
        return transportLayer;
    }

    public Server getServer() {
        return server;
    }

    public void setClientUrl(String clientUrl) {
        this.clientUrl = clientUrl;
    }

    public void setRegistrationEndpoint(String registrationEndpoint) {
        this.registrationEndpoint = registrationEndpoint;
    }

    public void setRegistrationInfo(RegistrationInfo registrationInfo) {
        this.registrationInfo = registrationInfo;
    }

    public RegistrationInfo getRegistrationInfo() {
        return registrationInfo;
    }
}
