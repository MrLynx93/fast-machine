package com.agh.fastmachine.server.internal.transport.operations;

import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;

public interface ReadOperations extends Operations {
    <T extends ObjectInstanceProxy> void read(ObjectBaseProxy<T> object);
    void read(ObjectInstanceProxy instance);
    void read(ObjectResourceProxy<?> resource);
}
