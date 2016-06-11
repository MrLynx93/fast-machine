package com.agh.fastmachine.server.internal.transport.operations;

import com.agh.fastmachine.core.internal.parser.WriteAttributesParser;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;
import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.internal.transport.TransportLayer;

public abstract class DiscoverOperations<L extends TransportLayer> extends AbstractOperations<L> {
    protected final WriteAttributesParser parser = new WriteAttributesParser();

    public DiscoverOperations(ClientProxyImpl clientProxy) {
        super(clientProxy);
    }

    public abstract <T extends ObjectInstanceProxy> void discover(ObjectBaseProxy<T> object);
    public abstract void discover(ObjectInstanceProxy instance);
    public abstract void discover(ObjectResourceProxy<?> resource);
}
