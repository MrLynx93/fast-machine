package com.agh.fastmachine.server.internal.transport.coap.operations;

import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.internal.transport.coap.CoapTransportLayer;
import com.agh.fastmachine.server.internal.transport.operations.AbstractOperations;
import com.agh.fastmachine.server.internal.transport.operations.DeleteOperations;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoapDeleteOperations extends DeleteOperations<CoapTransportLayer> {
    private static final Logger LOG = LoggerFactory.getLogger(CoapDeleteOperations.class);
    private CoapServer coapServer;
    private final int port;

    public CoapDeleteOperations(ClientProxyImpl clientProxy) {
        super(clientProxy);
        coapServer = transportLayer.getCoapServer();
        port = transportLayer.getPort();
    }

    @Override
    public void delete(ObjectInstanceProxy instance) {
        String endpoint = getEndpoint(instance);
        CoapClient coapClient = new CoapClient(endpoint);
        coapClient.setEndpoint(coapServer.getEndpoint(port));
        CoapResponse response = coapClient.delete();
        handleFailures(response, endpoint);

        if (response.isSuccess()) {
            LOG.debug("Deleted object: {}", endpoint);
        }
    }

    private void handleFailures(CoapResponse response, String requestEndpoint) {
        if (!response.isSuccess()) {
            LOG.error("Failed to delete object {} from client {}. Response code: {}", requestEndpoint, clientProxy.getClientEndpointName(), response.getCode());
        }
    }
}
