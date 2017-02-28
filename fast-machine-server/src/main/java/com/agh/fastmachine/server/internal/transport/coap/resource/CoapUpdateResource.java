package com.agh.fastmachine.server.internal.transport.coap.resource;

import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.internal.service.RegistrationService;
import com.agh.fastmachine.server.internal.service.registrationinfo.RegistrationInfo;
import com.agh.fastmachine.server.internal.transport.LWM2M;
import com.agh.fastmachine.server.internal.transport.stats.Event;
import com.agh.fastmachine.server.internal.transport.stats.Stats;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoapUpdateResource extends CoapResource {
    private static final Logger LOG = LoggerFactory.getLogger(CoapUpdateResource.class);
    private final Stats stats;
    private CoapRegistrationResource registrationResource;
    private RegistrationService registrationService;
    private ClientProxyImpl clientProxy;

    public CoapUpdateResource(String registrationEndpoint, Stats stats) {
        super(registrationEndpoint);
        this.stats = stats;
    }

    @Override
    public void handlePUT(CoapExchange exchange) {
        RegistrationInfo updatedInfo = registrationService.parseRegistrationInfo(exchange.advanced().getRequest());
        stats.addEvent(clientProxy, Event.uplinkRequestReceiveSuccess(LWM2M.Operation.R_UPDATE));
        registrationService.handleUpdateForClientProxy(clientProxy, updatedInfo);
        exchange.respond(CoAP.ResponseCode.CHANGED, "2.04 Changed");
        stats.addEvent(clientProxy, Event.uplinkResponseSendSuccess(LWM2M.Operation.R_UPDATE));
        registrationService.updateFinished(clientProxy);
    }

    @Override
    public void handleDELETE(CoapExchange exchange) {
        stats.addEvent(clientProxy, Event.uplinkRequestReceiveSuccess(LWM2M.Operation.R_DEREGISTER));
        registrationResource.delete(this);
        clientProxy.setRegistrationInfo(null);
        exchange.respond(CoAP.ResponseCode.DELETED);
        stats.addEvent(clientProxy, Event.uplinkResponseSendSuccess(LWM2M.Operation.R_DEREGISTER));
        LOG.debug("Deregistered client: {}", getName());
        registrationService.deregisterFinished(clientProxy);
    }

    public void setRegistrationService(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    public void setRegistrationResource(CoapRegistrationResource registrationResource) {
        this.registrationResource = registrationResource;
    }

    public void setClientProxy(ClientProxyImpl clientProxy) {
        this.clientProxy = clientProxy;
    }
}
