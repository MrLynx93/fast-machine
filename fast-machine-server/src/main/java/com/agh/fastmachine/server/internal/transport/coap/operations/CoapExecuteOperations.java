package com.agh.fastmachine.server.internal.transport.coap.operations;

import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;
import com.agh.fastmachine.server.internal.transport.coap.CoapTransportConfiguration;
import com.agh.fastmachine.server.internal.transport.coap.CoapTransportLayer;
import com.agh.fastmachine.server.internal.transport.operations.AbstractOperations;
import com.agh.fastmachine.server.internal.transport.operations.ExecuteOperations;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoapExecuteOperations extends ExecuteOperations<CoapTransportLayer> {
    private static final Logger LOG = LoggerFactory.getLogger(CoapExecuteOperations.class);
    private CoapServer coapServer;
    private int port;

    public CoapExecuteOperations(ClientProxyImpl clientProxy) {
        super(clientProxy);
        coapServer = transportLayer.getCoapServer();
        port = transportLayer.getPort();
    }

    @Override
    public void execute(ObjectResourceProxy<?> resource, byte[] arguments) {
        String endpoint = getEndpoint(resource);
        CoapClient coapClient = new CoapClient(endpoint);
        coapClient.setEndpoint(coapServer.getEndpoint(port));
        CoapResponse response = coapClient.post(arguments, MediaTypeRegistry.APPLICATION_LINK_FORMAT);
        handleFailures(response, endpoint);

        if (response.isSuccess()) {
            LOG.debug("Executed resource {}. Response text: {}", endpoint, response.getResponseText());
        }
    }

    private void handleFailures(CoapResponse response, String requestEndpoint) {
        if (!response.isSuccess()) {
            LOG.error("Failed to execute resource: {}. Response code: {}", requestEndpoint, clientProxy.getClientEndpointName(), response.getCode());
        }
    }
}
