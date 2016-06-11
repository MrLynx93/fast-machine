package com.agh.fastmachine.server.internal.transport.operations;

import com.agh.fastmachine.core.internal.parser.WriteParser;
import com.agh.fastmachine.server.api.ServerConfiguration;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.internal.transport.TransportLayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public abstract class CreateOperations<L extends TransportLayer> extends AbstractOperations<L> {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    public CreateOperations(ClientProxyImpl clientProxy) {
        super(clientProxy);
    }

    public abstract <T extends ObjectInstanceProxy> void create(T instance);
    public abstract <T extends ObjectInstanceProxy> void create(T instance, int instanceId);

    // TODO override getEndpoint?
    protected int getObjectIdForInstance(ObjectInstanceProxy instance) {
        ServerConfiguration serverConfiguration = clientProxy.getServer().getConfiguration();
        for (Map.Entry<Integer, Class<? extends ObjectInstanceProxy>> entry : serverConfiguration.getSupportedObjects().entrySet()) {
            if (instance.getClass().equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        LOG.error("Could not find object ID for instance. Check configuration");
        throw new IllegalStateException("Could not find objectId for instance: " + instance.getId());
    }
}
