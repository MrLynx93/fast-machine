package com.agh.fastmachine.server.internal.transport.coap.operations;

import com.agh.fastmachine.core.api.model.Attributes;
import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;
import com.agh.fastmachine.server.internal.transport.coap.CoapTransport;
import com.agh.fastmachine.server.internal.transport.operations.DiscoverOperations;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class CoapDiscoverOperations extends DiscoverOperations<CoapTransport> {
    private static final Logger LOG = LoggerFactory.getLogger(CoapDiscoverOperations.class);
    private CoapServer coapServer;
    private int port;

    public CoapDiscoverOperations(ClientProxyImpl clientProxy) {
        super(clientProxy);
        coapServer = transportLayer.getCoapServer();
        port = transportLayer.getPort();
    }

    @Override
    public <T extends ObjectInstanceProxy> void discover(ObjectBaseProxy<T> object) {
        String requestEndpoint = getEndpoint(object);
        CoapResponse response = doGetAndGetResponse(requestEndpoint);
        handleFailures(response, requestEndpoint);

        if (response.isSuccess()) {
            Attributes attributes = parser.parseWriteAttributes(response.getResponseText());
            if (attributes != null) {
                object.internal().updateAttributes(attributes);
            }

            List<Integer> supportedResources = parser.parseSupportedResources(response.getResponseText());

            for (T instance : object.getObjectInstances().values()) {
                for (Map.Entry<Integer, ? extends ObjectResourceProxy<?>> resourceEntry : instance.getResources().entrySet()) {
                    ObjectResourceProxy<?> resource = resourceEntry.getValue();
                    resource.internal().setSupported(supportedResources.contains(resourceEntry.getKey()));
                }
            }
            LOG.debug("Executed discover on object /{} on client {}", object.getId(), clientProxy.getClientEndpointName());
        }
    }

    @Override
    public void discover(ObjectInstanceProxy instance) {
        String endpoint = getEndpoint(instance);
        CoapResponse response = doGetAndGetResponse(endpoint);
        handleFailures(response, endpoint);

        if (response.isSuccess()) {
            Attributes attributes = parser.parseWriteAttributes(response.getResponseText());
            if (attributes != null) {
                instance.internal().updateAttributes(attributes);
            }
            LOG.debug("Executed discover on: {}", endpoint);
        }
    }

    @Override
    public void discover(ObjectResourceProxy<?> resource) {
        String endpoint = getEndpoint(resource);
        CoapResponse response = doGetAndGetResponse(endpoint);
        handleFailures(response, endpoint);

        if (response.isSuccess()) {
            Attributes attributes = parser.parseResourceWriteAttributes(response.getResponseText(), resource);
            if (attributes != null) {
                resource.internal().updateAttributes(attributes);
            }
            LOG.debug("Executed discover on: {}", endpoint);
        }
    }

    private CoapResponse doGetAndGetResponse(String requestEndpoint) {
        CoapClient coapClient = new CoapClient(requestEndpoint);
        coapClient.setEndpoint(coapServer.getEndpoint(port));
        return coapClient.get(MediaTypeRegistry.APPLICATION_LINK_FORMAT);
    }

    private void handleFailures(CoapResponse response, String requestEndpoint) {
        if (!response.isSuccess()) {
            LOG.error("Failed to discover: {}. Lwm2mResponse code: {}", requestEndpoint, response.getCode());
        }
    }

}
