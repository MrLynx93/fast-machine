package com.agh.fastmachine.client.internal.message.response;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class Lwm2mObserveResponse extends Lwm2mResponse {

    public Lwm2mObserveResponse() {
        super(CoAP.ResponseCode.CONTENT);
    }

    @Override
    public void respond(CoapExchange exchange) {
        Response response = new Response(code);
        response.setPayload(payload);
        response.setType(CoAP.Type.CON);
        exchange.respond(response);
    }
}
