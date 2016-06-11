package com.agh.fastmachine.client.internal.message.request;


import com.agh.fastmachine.client.api.model.builtin.ServerObjectInstance;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class Lwm2mContentRequest extends Lwm2mRequest {

    public Lwm2mContentRequest(CoapExchange exchange, ServerObjectInstance serverObjectInstance) {
        super(exchange, serverObjectInstance);
    }

    public byte[] getByteContent() {
        return exchange.getRequestPayload();
    }

    public String getStringContent() {
        return exchange.getRequestText();
    }
}
