package com.agh.fastmachine.server.internal.transport.coap.operations;

import com.agh.fastmachine.core.api.model.resourcevalue.OpaqueResourceValue;
import com.agh.fastmachine.core.internal.parser.WriteParser;
import com.agh.fastmachine.core.internal.MediaTypeRegistry;
import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectMultipleResourceProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;
import com.agh.fastmachine.server.internal.transport.coap.CoapTransport;
import com.agh.fastmachine.server.internal.transport.operations.WriteOperations;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoapWriteOperations extends WriteOperations<CoapTransport> {
    private static final Logger LOG = LoggerFactory.getLogger(CoapWriteOperations.class);
    private CoapServer coapServer;
    private int port;

    public CoapWriteOperations(ClientProxyImpl clientProxy) {
        super(clientProxy);
        coapServer = transportLayer.getCoapServer();
        port = transportLayer.getPort();
    }

    @Override
    public void write(ObjectInstanceProxy instance) {
        String endpoint = getEndpoint(instance);
        CoapClient coapClient = new CoapClient(endpoint);
        coapClient.setEndpoint(coapServer.getEndpoint(port));
        CoapResponse response = coapClient.put(WriteParser.serialize(instance), MediaTypeRegistry.TLV.getKey());
        handleFailures(response, endpoint);
        if (response.isSuccess()) {
            LOG.debug("Wrote to: {}", endpoint);
        }
    }

    @Override
    public void write(ObjectResourceProxy resource) {
        String endpoint = getEndpoint(resource);
        CoapClient coapClient = new CoapClient(endpoint);
        coapClient.setEndpoint(coapServer.getEndpoint(port));
        CoapResponse response = coapClient.put(WriteParser.serialize(resource), getUsedFormat(resource).getKey());
        handleFailures(response, endpoint);
        if (response.isSuccess()) {
            LOG.debug("Wrote to: {}", endpoint);
        }
    }

    private MediaTypeRegistry getUsedFormat(ObjectResourceProxy resource) {
        if (OpaqueResourceValue.class.equals(resource.getValueType())) {
            return MediaTypeRegistry.OPAQUE;
        }
        if (resource instanceof ObjectMultipleResourceProxy) {
            return MediaTypeRegistry.TLV;
        }
        return MediaTypeRegistry.PLAIN_TEXT;
    }

    private void handleFailures(CoapResponse response, String requestEndpoint) {
        if (!response.isSuccess()) {
            LOG.error("Failed to write to object {} on client {}. Lwm2mResponse code: {}", requestEndpoint, response.getCode());
        }
    }
}
