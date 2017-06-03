package com.agh.fastmachine.server.internal.client;

import com.agh.fastmachine.server.api.ClientProxy;
import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.internal.service.registrationinfo.RegistrationInfo;
import com.agh.fastmachine.server.api.model.ObjectTree;
import com.agh.fastmachine.server.internal.transport.Transport;
import org.eclipse.californium.core.network.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ClientProxyImpl extends BaseRegistrationListener implements ClientProxy {
    private static final Logger LOG = LoggerFactory.getLogger(ClientProxyImpl.class);

    private Transport transport;
    private ObjectTree objectTree;
    private ClientManager clientManager;
    private volatile ClientProxyStatus status;
    private String endpointClientName;
    private String registrationEndpoint;
    private String clientId;
    private String serverId;
    private String clientUrl;
    private final Server server;
    private RegistrationInfo registrationInfo;
    private Date lastUpdateTime;
    private Date registerTime;
    private KeepaliveThread keepaliveThread;
    private Endpoint clientEndpoint;


    public ClientProxyImpl(Server server, String endpointClientName) {
        this.server = server;
        this.clientManager = server.internal().getClientManager();
        this.endpointClientName = endpointClientName;
        this.status = ClientProxyStatus.CREATED;
        this.transport = server.internal().getTransportLayer();
        this.keepaliveThread = new KeepaliveThread(server.getName(), endpointClientName);
        this.keepaliveThread.start();
    }

    @Override
    public <T extends ObjectInstanceProxy> void create(T patternInstance) {
        patternInstance.internal().setClientProxy(this);
        transport.create(this, patternInstance);
    }

    @Override
    public <T extends ObjectInstanceProxy> void create(T patternInstance, int id) {
        patternInstance.internal().setClientProxy(this);
        patternInstance.internal().setId(id);
        transport.create(this, patternInstance, id);
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


    public void stopKeepaliveThread() {
        keepaliveThread.interrupt();
    }

    public void updateTimeout() {
        keepaliveThread.lock.lock();
        keepaliveThread.condition.signal();
        keepaliveThread.lock.unlock();
    }

    public void setClientEndpoint(Endpoint clientEndpoint) {
        this.clientEndpoint = clientEndpoint;
    }

    public Endpoint getClientEndpoint() {
        return clientEndpoint;
    }

    private class KeepaliveThread extends Thread {
        private Lock lock = new ReentrantLock();
        private Condition condition = lock.newCondition();
        private String serverName;
        private String clientName;

        public KeepaliveThread(String serverName, String clientName) {
            this.serverName = serverName;
            this.clientName = clientName;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    lock.lock();
                    if (!condition.await(getTimeout(), TimeUnit.MILLISECONDS)) {
                        // If timeout (no update came)
                        LOG.info("Didn't receive update. Removing client account. Client {}, server {}", clientName, serverName);
                        clientManager.removeClientForEndpointName(endpointClientName);
                        break;
                    }
                } catch (InterruptedException e) {
                    LOG.info("Interrupted client {} on server {} update thread. Probably deregistered", endpointClientName, ClientProxyImpl.this.server.getName());
                    break;
                } finally {
                    lock.unlock();
                }
            }
        }

        // TODO get lifetime from object model
        private long getTimeout() {
            return 1000000;
        }
    }
}
