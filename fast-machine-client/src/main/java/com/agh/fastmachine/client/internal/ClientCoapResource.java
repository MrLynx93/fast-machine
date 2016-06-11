package com.agh.fastmachine.client.internal;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class ClientCoapResource extends CoapResource {
    public ClientCoapResource(String name) {
        super(name);
    }

    @Override
    public void handleDELETE(CoapExchange exchange) {
        //TODO remove all instances except bootstrap server account
    }
}
