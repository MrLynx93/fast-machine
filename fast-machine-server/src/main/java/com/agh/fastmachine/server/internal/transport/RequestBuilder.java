package com.agh.fastmachine.server.internal.transport;

import com.agh.fastmachine.core.api.model.resourcevalue.OpaqueResourceValue;
import com.agh.fastmachine.server.api.model.*;

public abstract class RequestBuilder<REQ extends Lwm2mRequest> {
    public abstract REQ buildCreateRequest(ObjectInstanceProxy instance);
    public abstract REQ buildDeleteRequest(ObjectInstanceProxy instance);
    public abstract REQ buildDiscoverRequest(ObjectNodeProxy node);
    public abstract REQ buildExecuteRequest(ObjectResourceProxy<?> resource, byte[] arguments);
    public abstract REQ buildObserveRequest(ObjectNodeProxy<?> node);
    public abstract REQ buildCancelObserveRequest(ObjectNodeProxy<?> node);
    public abstract REQ buildReadRequest(ObjectBaseProxy<?> object);
    public abstract REQ buildReadRequest(ObjectInstanceProxy instance);
    public abstract REQ buildReadRequest(ObjectResourceProxy<?> resource);
    public abstract REQ buildWriteAttributesRequest(ObjectNodeProxy<?> node);
    public abstract REQ buildWriteRequest(ObjectInstanceProxy instance);
    public abstract REQ buildWriteRequest(ObjectResourceProxy resource);


    protected LWM2M.ContentType getResourceContentType(ObjectResourceProxy resource) {
        if (OpaqueResourceValue.class.equals(resource.getValueType())) {
            return LWM2M.ContentType.OPAQUE;
        }
        if (resource instanceof ObjectMultipleResourceProxy) {
            return LWM2M.ContentType.TLV;
        }
        return LWM2M.ContentType.PLAIN_TEXT;
    }

    protected LWM2M.ContentType getResourceFormat(ObjectResourceProxy resource) {
        if (OpaqueResourceValue.class.equals(resource.getValueType())) {
            return LWM2M.ContentType.OPAQUE;
        }
        if (resource instanceof ObjectMultipleResourceProxy) {
            return LWM2M.ContentType.TLV;
        }
        return LWM2M.ContentType.PLAIN_TEXT;
    }

    public String generateToken() {
        return null; // TODO
    }
}
