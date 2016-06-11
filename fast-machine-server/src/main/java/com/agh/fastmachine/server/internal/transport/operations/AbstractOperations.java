package com.agh.fastmachine.server.internal.transport.operations;

import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;
import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.internal.transport.TransportLayer;

public abstract class AbstractOperations<L extends TransportLayer> {
    protected final ClientProxyImpl clientProxy;
    protected final L transportLayer;
    protected final Server server;

    public AbstractOperations(ClientProxyImpl clientProxy) {
        this.clientProxy = clientProxy;
        this.transportLayer = (L) clientProxy.getServer().internal().getTransportLayer();
        this.server = clientProxy.getServer();
    }

    protected String getEndpoint(ObjectBaseProxy objectBase) {
        return clientProxy.getClientUrl() + "/" + objectBase.getId();
    }

    protected String getEndpoint(ObjectInstanceProxy objectInstance) {
        return clientProxy.getClientUrl() + "/" + objectInstance.getObject().getId() + "/" + objectInstance.getId();
    }

    protected String getEndpoint(ObjectResourceProxy objectResource) {
        return clientProxy.getClientUrl() + "/" + objectResource.getInstance().getObject().getId() + "/" + objectResource.getInstance().getId() + "/" + objectResource.getId();
    }
}
