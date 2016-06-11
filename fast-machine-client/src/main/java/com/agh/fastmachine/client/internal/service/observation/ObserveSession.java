package com.agh.fastmachine.client.internal.service.observation;

import com.agh.fastmachine.client.internal.attribute.AttributesResolver;
import com.agh.fastmachine.core.api.model.Attributes;
import com.agh.fastmachine.client.api.model.AbstractLwm2mNode;
import com.agh.fastmachine.client.api.model.Lwm2mNodeCoapResource;
import com.agh.fastmachine.client.api.model.builtin.ServerObjectInstance;
import org.eclipse.californium.core.server.resources.CoapExchange;

public final class ObserveSession {
    public long lastNotifyTime = 0;
    public ServerObjectInstance server;
    public AbstractLwm2mNode node;
    public CoapExchange exchange;
    public Lwm2mNodeCoapResource coapResource;

    public ObserveSession(ServerObjectInstance server, AbstractLwm2mNode node, CoapExchange exchange) {
        this.server = server;
        this.node = node;
        this.exchange = exchange;
        this.coapResource = (Lwm2mNodeCoapResource) exchange.advanced().getRelation().getResource();
    }

    public Attributes getMergedWriteAttributes() {
        return AttributesResolver.getMergedAttributes(node, server.getShortServerId());
    }

    public long getTimeSinceLastNotify() {
        return System.currentTimeMillis() - lastNotifyTime;
    }

    public String getServerUri() {
        return exchange.getSourceAddress().getHostAddress() + exchange.getSourcePort();
    }
}
