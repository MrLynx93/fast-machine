package com.agh.fastmachine.server.api.model;

import com.agh.fastmachine.core.api.model.Attributes;
import com.agh.fastmachine.core.internal.model.ObjectNodeModel;
import com.agh.fastmachine.server.api.ClientProxy;
import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.internal.transport.LWM2M;
import com.agh.fastmachine.server.internal.transport.Transport;

public abstract class ObjectNodeProxy<I extends ObjectNodeProxy.Internal> implements ObjectNodeModel {
    protected Transport transport;
    protected ClientProxyImpl clientProxy;
    protected Attributes attributes;
    protected I internal;
    protected int id;
    private boolean supported;
    private String observeToken;

    public ObjectNodeProxy() {
    }

    public ObjectNodeProxy(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    public abstract void writeAttributes();

    public abstract LWM2M.Path getPath();

    public Attributes getAttributes() {
        return attributes;
    }

    public ClientProxy getClientProxy() {
        return clientProxy;
    }

    public boolean isSupported() {
        return supported;
    }

    /// INTERNAL ///

    public I internal() {
        return internal;
    }

    public String getObserveToken() {
        return observeToken;
    }

    public void setObserveToken(String observeToken) {
        this.observeToken = observeToken;
    }

    public class Internal {
        Internal() {
        }

        public void updateAttributes(Attributes attributes) {
            ObjectNodeProxy.this.attributes = attributes;
        }


        public void setTransportLayer(Transport transport) {
            ObjectNodeProxy.this.transport = transport;
        }

        public void setSupported(boolean supported) {
            ObjectNodeProxy.this.supported = supported;
        }

        public void setClientProxy(ClientProxy clientProxy) {
            ObjectNodeProxy.this.clientProxy = (ClientProxyImpl) clientProxy;
        }
    }

}
