package com.agh.fastmachine.server.internal.transport.coap.operations;

import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;
import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.internal.transport.coap.CoapTransportLayer;
import com.agh.fastmachine.server.internal.transport.operations.WriteAttributeOperations;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoapWriteAttributeOperations extends WriteAttributeOperations<CoapTransportLayer> {
    private static final Logger LOG = LoggerFactory.getLogger(CoapWriteAttributeOperations.class);
    private CoapServer coapServer;
    private int port;

    public CoapWriteAttributeOperations(ClientProxyImpl clientProxy) {
        super(clientProxy);
        coapServer = transportLayer.getCoapServer();
        port = transportLayer.getPort();
    }

    @Override
    public <T extends ObjectInstanceProxy> void writeAttributes(ObjectBaseProxy<T> object) {
        String endpoint = getEndpoint(object) + "?" + object.getAttributes().toRestString();
        writeNodeAttributes(endpoint);
    }

    @Override
    public void writeAttributes(ObjectInstanceProxy instance) {
        String endpoint = getEndpoint(instance) + "?" + instance.getAttributes().toRestString();
        writeNodeAttributes(endpoint);
    }

    @Override
    public void writeAttributes(ObjectResourceProxy resource) {
        String endpoint = getEndpoint(resource) + "?" + resource.getAttributes().toRestString();
        writeNodeAttributes(endpoint);
    }

    private void writeNodeAttributes(String endpoint) {
        CoapClient coapClient = new CoapClient(endpoint);
        coapClient.setEndpoint(coapServer.getEndpoint(port));
        CoapResponse response = coapClient.put("", MediaTypeRegistry.APPLICATION_LINK_FORMAT);
        handleFailures(response, endpoint);
        if (response.isSuccess()) {
            LOG.debug("Write attributes to: {}", endpoint);
        }
    }

    private void handleFailures(CoapResponse response, String requestEndpoint) {
        if (!response.isSuccess()) {
            LOG.error("Failed to write attributes to {} on client {}. Response code: {}", requestEndpoint, clientProxy.getClientEndpointName(), response.getCode());
        }
    }
}
