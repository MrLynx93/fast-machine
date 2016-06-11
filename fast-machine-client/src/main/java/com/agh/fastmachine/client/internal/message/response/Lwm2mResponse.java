package com.agh.fastmachine.client.internal.message.response;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class Lwm2mResponse {
    public CoAP.ResponseCode code;
    public byte[] payload;
    public CoAP.Type type;

    public Lwm2mResponse(CoAP.ResponseCode code) {
        this.code = code;
    }

    public Lwm2mResponse(CoAP.ResponseCode code, byte[] payload) {
        this.code = code;
        this.payload = payload;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void respond(CoapExchange exchange) {
        Response response = new Response(code);
        response.setPayload(payload);
        exchange.respond(response);
    }
}
