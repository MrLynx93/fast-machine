package com.agh.fastmachine.server.internal.transport.coap.message;

import com.agh.fastmachine.core.api.model.Attributes;
import com.agh.fastmachine.server.internal.transport.LWM2M;
import lombok.Data;

@Data
public class Lwm2mCoapWriteAttributesRequest extends Lwm2mCoapRequest {
    private final Attributes attributes;

    Lwm2mCoapWriteAttributesRequest(LWM2M.Path path, LWM2M.Operation operation, String token, LWM2M.ContentType contentType, Attributes attributes) {
        super(path, operation, token, contentType, null);
        this.attributes = attributes;
    }

    @Override
    public String getCoapPath() {
        return super.getCoapPath() + "?" + attributes.toRestString();
    }
}
