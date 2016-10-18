package com.agh.fastmachine.server.internal.transport.operations;

import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;

public interface DiscoverOperations extends Operations {
    <T extends ObjectInstanceProxy> void discover(ObjectBaseProxy<T> object);
    void discover(ObjectInstanceProxy instance);
    void discover(ObjectResourceProxy<?> resource);
}
