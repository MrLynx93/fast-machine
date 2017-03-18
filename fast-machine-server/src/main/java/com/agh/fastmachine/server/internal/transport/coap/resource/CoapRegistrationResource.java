package com.agh.fastmachine.server.internal.transport.coap.resource;

import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.internal.client.ClientManager;
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

import java.net.InetSocketAddress;

public class CoapRegistrationResource extends CoapResource {
    private static final Logger LOG = LoggerFactory.getLogger(CoapRegistrationResource.class);
    private final RegistrationService registrationService;
    private final ClientManager clientManager;
    private Stats stats;

    public CoapRegistrationResource(Server server, Stats stats) {
        super("rd");
        this.registrationService = server.internal().getRegistrationService();
        this.clientManager = server.internal().getClientManager();
        this.stats = stats;
    }

    @Override
    public void handlePOST(CoapExchange exchange) {
        RegistrationInfo registrationInfo = registrationService.parseRegistrationInfo(exchange.advanced().getRequest());
        String endpointClientName = registrationInfo.endpointClientName;
        String clientUrl = getClientUrl(exchange);

        if (!registrationService.isClientRegistered(endpointClientName)) {
            ClientProxyImpl clientProxy = clientManager.createClient(endpointClientName);
            stats.addEvent(clientProxy, Event.uplinkRequestReceiveSuccess(LWM2M.Operation.R_REGISTER));
            String registrationEndpoint = registrationService.registerClientProxy(clientProxy, registrationInfo);
            createUpdateCoapResource(clientProxy, registrationEndpoint);

            clientProxy.setClientUrl(clientUrl);
            clientProxy.setClientEndpoint(exchange.advanced().getEndpoint());

            exchange.setLocationPath("/rd/" + clientProxy.getRegistrationEndpoint());
            exchange.respond(CoAP.ResponseCode.CREATED);
            stats.addEvent(clientProxy, Event.uplinkResponseSendSuccess(LWM2M.Operation.R_REGISTER));
            clientProxy.onRegister(clientProxy);
        }
    }

    private void createUpdateCoapResource(ClientProxyImpl clientProxy, String registrationEndpoint) {
        CoapUpdateResource resource = new CoapUpdateResource(registrationEndpoint, stats);
        resource.setRegistrationService(registrationService);
        resource.setRegistrationResource(this);
        resource.setClientProxy(clientProxy);
        this.add(resource);
    }

    private String getClientUrl(CoapExchange exchange) {
        InetSocketAddress peerAddress = new InetSocketAddress(exchange.getSourceAddress(), exchange.getSourcePort());
        return String.format("coap://%s:%d", peerAddress.getHostName(), peerAddress.getPort());
    }
}
