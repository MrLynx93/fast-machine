package com.agh.fastmachine.server.internal.transport.coap.resource;

import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.internal.client.ClientManager;
import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.internal.service.BootstrapService;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CoapBootstrapResource extends CoapResource {
    private static final Logger LOG = LoggerFactory.getLogger(CoapBootstrapResource.class);
    private final BootstrapService bootstrapService;
    private final ClientManager clientManager;

    public CoapBootstrapResource(Server server) {
        super("bs");
        this.clientManager = server.internal().getClientManager();
        this.bootstrapService = server.internal().getBootstrapService();
    }

    @Override
    public void handlePOST(CoapExchange exchange) {
        String endpointClientName = extractEndpointClientNameFromRequest(exchange.advanced().getRequest());
        ClientProxyImpl clientProxy = clientManager.createClient(endpointClientName);
        bootstrapService.bootstrapClientProxy(clientProxy);
        exchange.respond(CoAP.ResponseCode.CHANGED);
        LOG.debug("Bootstrapped client: {}", endpointClientName);
    }

    private String extractEndpointClientNameFromRequest(Request request) {
        List<String> queryParams = request.getOptions().getUriQuery();
        String firstParam = queryParams.iterator().next();
        return firstParam.split("=")[1];
    }

}
