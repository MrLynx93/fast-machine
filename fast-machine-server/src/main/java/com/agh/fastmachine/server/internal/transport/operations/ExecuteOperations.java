package com.agh.fastmachine.server.internal.transport.operations;

import com.agh.fastmachine.server.api.model.ObjectResourceProxy;
import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.internal.transport.TransportLayer;

public abstract class ExecuteOperations<L extends TransportLayer> extends AbstractOperations<L> {

    public ExecuteOperations(ClientProxyImpl clientProxy) {
        super(clientProxy);
    }

    public abstract void execute(ObjectResourceProxy<?> resource, byte[] arguments);
}
