package com.agh.fastmachine.server.internal.transport.operations;

import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;
import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.internal.transport.TransportLayer;

public abstract class WriteOperations<L extends TransportLayer> extends AbstractOperations<L> {

    public WriteOperations(ClientProxyImpl clientProxy) {
        super(clientProxy);
    }

    public abstract void write(ObjectInstanceProxy instance);
    public abstract void write(ObjectResourceProxy resource);
}
