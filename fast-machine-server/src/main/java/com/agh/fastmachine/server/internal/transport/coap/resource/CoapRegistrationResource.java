package com.agh.fastmachine.server.internal.transport.coap.resource;

import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.internal.client.ClientManager;
import com.agh.fastmachine.server.internal.service.RegistrationService;
import com.agh.fastmachine.server.internal.service.registrationinfo.RegistrationInfo;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class CoapRegistrationResource extends CoapResource {
    private final RegistrationService registrationService;
    private final ClientManager clientManager;

    public CoapRegistrationResource(Server server) {
        super("rd");
        this.registrationService = server.internal().getRegistrationService();
        this.clientManager = server.internal().getClientManager();
    }

    @Override
    public void handlePOST(CoapExchange exchange) {
        RegistrationInfo registrationInfo = registrationService.parseRegistrationInfo(exchange.advanced().getRequest());
        String endpointClientName = registrationInfo.endpointClientName;
        String clientUrl = getClientUrl(exchange);

        if (!registrationService.isClientRegistered(endpointClientName)) {
            ClientProxyImpl clientProxy = clientManager.createClient(endpointClientName);
            String registrationEndpoint = registrationService.registerClientProxy(clientProxy, registrationInfo);
            createUpdateCoapResource(clientProxy, registrationEndpoint);

            clientProxy.setClientUrl(clientUrl);

            exchange.setLocationPath("/rd/" + clientProxy.getRegistrationEndpoint());
            exchange.respond(CoAP.ResponseCode.CREATED);
            clientProxy.onRegister(clientProxy);
        }
    }

    private void createUpdateCoapResource(ClientProxyImpl clientProxy, String registrationEndpoint) {
        CoapUpdateResource resource = new CoapUpdateResource(registrationEndpoint);
        resource.setRegistrationService(registrationService);
        resource.setRegistrationResource(this);
        resource.setClientProxy(clientProxy);
        this.add(resource);
    }

    private String getClientUrl(CoapExchange exchange) {
        String clientAddress = exchange.getSourceAddress().getHostName();
        int clientPort = exchange.getSourcePort();
        return String.format("coap://%s:%d", clientAddress, clientPort);
    }
}
