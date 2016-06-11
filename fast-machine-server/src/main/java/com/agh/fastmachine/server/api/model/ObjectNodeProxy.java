package com.agh.fastmachine.server.api.model;

import com.agh.fastmachine.core.api.model.Attributes;
import com.agh.fastmachine.core.internal.model.ObjectNodeModel;
import com.agh.fastmachine.server.api.ClientProxy;
import com.agh.fastmachine.server.internal.transport.TransportLayer;

public abstract class ObjectNodeProxy<I extends ObjectNodeProxy.Internal> implements ObjectNodeModel {
    protected TransportLayer transportLayer;
    protected ClientProxy clientProxy;
    protected Attributes attributes;
    protected I internal;
    protected int id;
    private boolean supported;

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

    public class Internal {
        Internal() {
        }

        public void updateAttributes(Attributes attributes) {
            ObjectNodeProxy.this.attributes = attributes;
        }


        public void setTransportLayer(TransportLayer transportLayer) {
            ObjectNodeProxy.this.transportLayer = transportLayer;
        }

        public void setSupported(boolean supported) {
            ObjectNodeProxy.this.supported = supported;
        }

        public void setClientProxy(ClientProxy clientProxy) {
            ObjectNodeProxy.this.clientProxy = clientProxy;
        }
    }

}
