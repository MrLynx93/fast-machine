package com.agh.fastmachine.server.internal.transport.operations;

import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;

public interface WriteAttributeOperations extends Operations {
    <T extends ObjectInstanceProxy> void writeAttributes(ObjectBaseProxy<T> object);
    void writeAttributes(ObjectInstanceProxy instance);
    void writeAttributes(ObjectResourceProxy resource);
}
