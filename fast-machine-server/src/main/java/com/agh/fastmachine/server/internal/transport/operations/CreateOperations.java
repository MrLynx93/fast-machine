package com.agh.fastmachine.server.internal.transport.operations;

import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.internal.client.ClientProxyImpl;

public interface CreateOperations extends Operations {
    <T extends ObjectInstanceProxy> void create(T instance, ClientProxyImpl client);
    <T extends ObjectInstanceProxy> void create(T instance, ClientProxyImpl client, int instanceId);
}
