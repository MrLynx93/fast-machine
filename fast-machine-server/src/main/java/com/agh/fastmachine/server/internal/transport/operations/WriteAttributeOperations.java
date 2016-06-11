package com.agh.fastmachine.server.internal.transport.operations;

import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;
import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.internal.transport.TransportLayer;

public abstract class WriteAttributeOperations<L extends TransportLayer> extends AbstractOperations<L> {

    public WriteAttributeOperations(ClientProxyImpl clientProxy) {
        super(clientProxy);
    }

    public abstract <T extends ObjectInstanceProxy> void writeAttributes(ObjectBaseProxy<T> object);
    public abstract void writeAttributes(ObjectInstanceProxy instance);
    public abstract void writeAttributes(ObjectResourceProxy resource);
}
