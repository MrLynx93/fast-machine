package com.agh.fastmachine.server.internal.transport.operations;

import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;

public interface WriteOperations extends Operations {
    void write(ObjectInstanceProxy instance);
    void write(ObjectResourceProxy resource);
}
