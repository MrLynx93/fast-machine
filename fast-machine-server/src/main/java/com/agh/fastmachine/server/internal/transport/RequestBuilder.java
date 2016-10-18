package com.agh.fastmachine.server.internal.transport;

import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectNodeProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;

public interface RequestBuilder<REQ extends Lwm2mRequest> {
    REQ buildCreateRequest(ObjectInstanceProxy instance);
    REQ buildDeleteRequest(ObjectInstanceProxy instance);
    REQ buildDiscoverRequest(ObjectNodeProxy node);
    REQ buildExecuteRequest(ObjectResourceProxy<?> resource, byte[] arguments);
    REQ buildObserveRequest(ObjectNodeProxy<?> node);
    REQ buildReadRequest(ObjectBaseProxy<?> object);
    REQ buildReadRequest(ObjectInstanceProxy instance);
    REQ buildReadRequest(ObjectResourceProxy<?> resource);
    REQ buildWriteAttributesRequest(ObjectNodeProxy<?> node);
    REQ buildWriteRequest(ObjectInstanceProxy instance);
    REQ buildWriteRequest(ObjectResourceProxy resource);
}
