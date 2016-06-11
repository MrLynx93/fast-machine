package com.agh.fastmachine.server.internal.transport;

import com.agh.fastmachine.server.api.ClientProxy;
import com.agh.fastmachine.server.internal.transport.operations.*;

public abstract class TransportLayer<T extends TransportConfiguration> {
    protected T configuration;

    public abstract void start(T configuration);
    public abstract void stop();

    public abstract ObserveOperations observeOperations(ClientProxy clientProxy);
    public abstract CreateOperations createOperations(ClientProxy clientProxy);
    public abstract ReadOperations readOperations(ClientProxy clientProxy);
    public abstract WriteOperations writeOperations(ClientProxy clientProxy);
    public abstract DiscoverOperations discoverOperations(ClientProxy clientProxy);
    public abstract WriteAttributeOperations writeAttributeOperations(ClientProxy clientProxy);
    public abstract DeleteOperations deleteOperations(ClientProxy clientProxy);
    public abstract ExecuteOperations executeOperations(ClientProxy clientProxy);

}
