package com.agh.fastmachine.server.internal.transport.operations;

import com.agh.fastmachine.core.internal.parser.ReadParser;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;
import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.internal.parser.ServerReadParser;
import com.agh.fastmachine.server.internal.transport.TransportLayer;

public abstract class ReadOperations<L extends TransportLayer> extends AbstractOperations<L> {
    protected final ReadParser readParser = new ServerReadParser();

    public ReadOperations(ClientProxyImpl clientProxy) {
        super(clientProxy);
    }

    public abstract <T extends ObjectInstanceProxy> void read(ObjectBaseProxy<T> object);
    public abstract void read(ObjectInstanceProxy instance);
    public abstract void read(ObjectResourceProxy<?> resource);
}
