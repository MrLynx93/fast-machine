package com.agh.fastmachine.server.internal.client;

import com.agh.fastmachine.server.api.ClientProxy;
import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.internal.service.registrationinfo.RegistrationInfo;
import com.agh.fastmachine.server.api.model.ObjectTree;
import com.agh.fastmachine.server.internal.transport.Transport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Date;

public class ClientProxyImpl extends BaseRegistrationListener implements ClientProxy {
    private static final Logger LOG = LoggerFactory.getLogger(ClientProxyImpl.class);

    private Transport transport;
    private ObjectTree objectTree;
    private ClientProxyStatus status;
    private String endpointClientName;
    private String registrationEndpoint;
    private String clientId;
    private String serverId;
    private String clientUrl;
    private final Server server;
    private RegistrationInfo registrationInfo;
    private Date lastUpdateTime;
    private Date registerTime;

    public ClientProxyImpl(Server server, String endpointClientName) {
        this.server = server;
        this.endpointClientName = endpointClientName;
        this.status = ClientProxyStatus.CREATED;
        this.transport = server.internal().getTransportLayer();
    }

    @Override
    public <T extends ObjectInstanceProxy> void create(T patternInstance) {
        patternInstance.internal().setClientProxy(this);
        transport.create(patternInstance);
    }

    @Override
    public <T extends ObjectInstanceProxy> void create(T patternInstance, int id) {
        patternInstance.internal().setClientProxy(this);
        patternInstance.internal().setId(id);
        transport.create(patternInstance, id);
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
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

    @Override
    public Date getLastUpdateTime() {
        return this.lastUpdateTime;
    }

    public void setLastUpdateTime(Date time) {
        this.lastUpdateTime = time;
    }

    @Override
    public Date getRegisterTime() {
        return this.lastUpdateTime;
    }

    public void setRegisterTime(Date time) {
        this.registerTime = time;
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

    public Transport getTransport() {
        return transport;
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
