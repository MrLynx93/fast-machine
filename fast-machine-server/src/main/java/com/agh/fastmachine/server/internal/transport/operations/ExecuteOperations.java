package com.agh.fastmachine.server.internal.transport.operations;

import com.agh.fastmachine.server.api.model.ObjectResourceProxy;

public interface ExecuteOperations extends Operations {
    void execute(ObjectResourceProxy<?> resource, byte[] arguments);
}
