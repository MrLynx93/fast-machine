package com.agh.fastmachine.client.internal.message.request;

import com.agh.fastmachine.client.api.model.builtin.ServerObjectInstance;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.util.List;

public class Lwm2mRequest {

    protected CoapExchange exchange;

    private ServerObjectInstance sender;

    public Lwm2mRequest(CoapExchange exchange, ServerObjectInstance sender) {
        this.exchange = exchange;
        this.sender = sender;
    }

    public int getContentType() {
        return exchange.getRequestOptions().getContentFormat();
    }

    public CoapExchange getExchange() {
        return exchange;
    }

    public ServerObjectInstance getRequestingServer() {
        return sender;
    }

    public List<String> getPath() {
        return exchange.getRequestOptions().getUriPath();
    }

    public int getAcceptType() {
        return exchange.getRequestOptions().getAccept();
    }

    public String getServerUri() {
        return exchange.getSourceAddress().getHostAddress() + exchange.getSourcePort();
    }

}
