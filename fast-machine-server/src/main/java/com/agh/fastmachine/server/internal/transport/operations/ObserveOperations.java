package com.agh.fastmachine.server.internal.transport.operations;

import com.agh.fastmachine.core.api.model.resourcevalue.ResourceValue;
import com.agh.fastmachine.server.api.listener.ObservationListener;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;
import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.internal.parser.ServerReadParser;
import com.agh.fastmachine.server.internal.transport.TransportLayer;

public abstract class ObserveOperations<L extends TransportLayer> extends AbstractOperations<L> {
    protected final ServerReadParser readParser = new ServerReadParser();

    public ObserveOperations(ClientProxyImpl clientProxy) {
        super(clientProxy);
    }

    public abstract <T extends ObjectInstanceProxy> void observe(ObjectBaseProxy<T> object, ObservationListener<ObjectBaseProxy<T>> listener);
    public abstract void observe(ObjectInstanceProxy instance, ObservationListener<ObjectInstanceProxy> listener);
    public abstract <T extends ResourceValue<?>> void observe(ObjectResourceProxy<T> resource, ObservationListener<ObjectResourceProxy<T>> listener);
    public abstract void cancelObservation(ObjectBaseProxy object);
    public abstract void cancelObservation(ObjectInstanceProxy instance);
    public abstract void cancelObservation(ObjectResourceProxy resource);
}
