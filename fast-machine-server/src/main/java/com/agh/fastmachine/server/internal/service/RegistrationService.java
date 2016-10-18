package com.agh.fastmachine.server.internal.service;

import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.model.ObjectTree;
import com.agh.fastmachine.server.internal.ObjectTreeCreator;
import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.internal.client.ClientProxyStatus;
import com.agh.fastmachine.server.internal.service.registrationinfo.RegistrationInfo;
import com.agh.fastmachine.server.internal.transport.coap.CoapTransport;
import org.eclipse.californium.core.coap.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistrationService {
    private static final Logger LOG = LoggerFactory.getLogger(RegistrationService.class);
    private static int t = 0;
    private Server server;
    private ObjectTreeCreator objectTreeCreator;

    public RegistrationService(Server server) {
        this.server = server;
        this.objectTreeCreator = server.internal().getObjectTreeCreator();
    }

    public String registerClientProxy(ClientProxyImpl clientProxy, RegistrationInfo registrationInfo) {
        String registrationEndpoint = getNextUpdateUrl();
        ObjectTree objectTree = objectTreeCreator.createObjectTree(clientProxy, registrationInfo.objects);
        clientProxy.setRegistrationEndpoint(registrationEndpoint);
        clientProxy.setRegistrationInfo(registrationInfo);
        clientProxy.setObjectTree(objectTree);
        clientProxy.setStatus(ClientProxyStatus.REGISTERED);
        return registrationEndpoint;
    }

    public void handleUpdateForClientProxy(ClientProxyImpl clientProxy, RegistrationInfo updatedInfo) {
        RegistrationInfo registrationInfo = clientProxy.getRegistrationInfo();
        registrationInfo.bindingMode = updatedInfo.bindingMode;
        registrationInfo.smsNumber = updatedInfo.smsNumber;
        registrationInfo.lifetime = updatedInfo.lifetime;
        clientProxy.setStatus(ClientProxyStatus.REGISTERED);
    }

    public boolean isClientRegistered(String endpointClientName) {
        return server.internal().getClientManager().getClientForEndpointName(endpointClientName) != null;
    }

    public void updateFinished(ClientProxyImpl clientProxy) {
        clientProxy.onUpdate(clientProxy);
    }

    public void deregisterFinished(ClientProxyImpl clientProxy) {
        clientProxy.setStatus(ClientProxyStatus.BOOTSTRAPPED);
        clientProxy.onDeregister(clientProxy);
    }

    public RegistrationInfo parseRegistrationInfo(Request request) {
        return ((CoapTransport)server.internal().getTransportLayer()).parseRegistrationInfo(request);
    }

    private String getNextUpdateUrl() {
        return "node" + t++;
    }

}
