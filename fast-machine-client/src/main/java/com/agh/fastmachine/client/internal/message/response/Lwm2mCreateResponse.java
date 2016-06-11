package com.agh.fastmachine.client.internal.message.response;

import com.agh.fastmachine.client.api.model.ObjectInstance;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class Lwm2mCreateResponse<T extends ObjectInstance> extends Lwm2mResponse {
    private final String locationPath;
    private T instance;

    public Lwm2mCreateResponse(CoAP.ResponseCode code) {
        this(code, "");
    }

    public Lwm2mCreateResponse(CoAP.ResponseCode code, String locationPath) {
        super(code);
        this.locationPath = locationPath;
    }

    public Lwm2mCreateResponse(CoAP.ResponseCode code, byte[] content, T instance) {
        super(code, content);
        this.instance = instance;
        this.locationPath = "/" + instance.parent().getId() + "/" + instance.getId();
    }

    public T getInstance() {
        return instance;
    }

    @Override
    public void respond(CoapExchange exchange) {
        exchange.setLocationPath(locationPath);
        super.respond(exchange);
    }
}
