package com.agh.fastmachine.client.api.model;

import com.agh.fastmachine.client.internal.message.request.Lwm2mContentRequest;
import com.agh.fastmachine.client.internal.message.request.Lwm2mRequest;
import com.agh.fastmachine.client.internal.message.request.Lwm2mWriteAttributeRequest;
import com.agh.fastmachine.client.internal.message.response.Lwm2mNotifyResponse;
import com.agh.fastmachine.client.internal.message.response.Lwm2mResponse;
import com.agh.fastmachine.client.api.model.builtin.ServerObjectInstance;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO move to internal, (change package-local methods??)
public class Lwm2mNodeCoapResource extends CoapResource {
    private static final Logger LOG = LoggerFactory.getLogger(Lwm2mNodeCoapResource.class);
    private final AbstractLwm2mNode node;
    private Lwm2mNotifyResponse pendingNotify;

    public Lwm2mNodeCoapResource(AbstractLwm2mNode node) {
        super(String.valueOf(node.getId()));
        this.node = node;
        this.setObservable(true);
        node.setCoapResource(this);
    }

    @Override
    public void handleGET(CoapExchange exchange) {
        ServerObjectInstance server = getRequestingServer(exchange);
        Lwm2mRequest request = new Lwm2mRequest(exchange, server);
        Lwm2mResponse response;

        if (exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_LINK_FORMAT) {
            response = node.handleDiscover(request);
        } else if (exchange.getRequestOptions().hasObserve()) {
            if (node.isObserved(server.shortServerId.getValue().value)) {
                response = pendingNotify;
            } else {
                response = node.handleObserve(request);
            }
        } else {
            response = node.handleRead(request);
        }
        response.respond(exchange);
    }

    @Override
    public void handleDELETE(CoapExchange exchange) {
        ServerObjectInstance server = getRequestingServer(exchange);
        Lwm2mResponse lwm2mResponse = (((ObjectInstance) this.node).parent()).handleDelete(new Lwm2mRequest(exchange, server));
        lwm2mResponse.respond(exchange);
    }

    @Override
    public void handlePUT(CoapExchange exchange) {
        System.out.println("handlePUT. exchange port: " + exchange.getSourcePort());
        Lwm2mResponse lwm2mResponse;
        ServerObjectInstance resolve = getRequestingServer(exchange);
        if (exchange.getRequestOptions().getURIQueryCount() != 0) {
            lwm2mResponse = node.handleWriteAttributes(new Lwm2mWriteAttributeRequest(exchange, resolve));
        } else {
            System.out.println("handleWRITE");
            lwm2mResponse = node.handleWrite(new Lwm2mContentRequest(exchange, resolve));
        }
        lwm2mResponse.respond(exchange);
    }

    @Override
    public void handlePOST(CoapExchange exchange) {
        Lwm2mResponse lwm2mResponse;
        ServerObjectInstance resolve = getRequestingServer(exchange);
        if (exchange.getRequestOptions().getURIPathCount() == 3) { // - /0/0/0 - resourcevalue path, therefore handle executelwm2mResponse = node.handleExecute();
            lwm2mResponse = node.handleExecute(new Lwm2mContentRequest(exchange, resolve));
        } else {
            Integer requestedId = exchange.getRequestOptions().getURIPathCount() == 2 ? Integer.parseInt(exchange.getRequestOptions().getUriPath().get(1)) : null;
            lwm2mResponse = node.handleCreate(new Lwm2mContentRequest(exchange, resolve), requestedId);
        }
        lwm2mResponse.respond(exchange);
    }

    @Override
    public Resource getChild(String name) {
        Resource child = super.getChild(name);
        if (child != null) {
            return child;
        }
        return this; // request to create children or invalid request
    }

    public void changed(Lwm2mNotifyResponse response) {
        this.pendingNotify = response;
        super.changed();
    }

    private ServerObjectInstance getRequestingServer(CoapExchange exchange) {
        return node.getClient().getServerObjectResolver().resolveServerObject(exchange);
    }
}
