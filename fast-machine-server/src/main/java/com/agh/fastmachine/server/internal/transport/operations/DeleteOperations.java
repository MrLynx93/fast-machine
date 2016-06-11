package com.agh.fastmachine.server.internal.transport.operations;

import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.internal.transport.TransportLayer;

public abstract class DeleteOperations<L extends TransportLayer> extends AbstractOperations<L> {

    public DeleteOperations(ClientProxyImpl clientProxy) {
        super(clientProxy);
    }

    public abstract void delete(ObjectInstanceProxy instance);

}
