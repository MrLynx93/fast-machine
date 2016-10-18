package com.agh.fastmachine.server.internal.transport.operations;

import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;

public interface DeleteOperations extends Operations {
    void delete(ObjectInstanceProxy instance);
}
