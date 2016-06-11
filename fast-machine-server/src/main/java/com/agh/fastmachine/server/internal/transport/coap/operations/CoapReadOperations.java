package com.agh.fastmachine.server.internal.transport.coap.operations;

import com.agh.fastmachine.core.api.model.resourcevalue.OpaqueResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.ResourceValue;
import com.agh.fastmachine.core.internal.MediaTypeRegistry;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectMultipleResourceProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;
import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.internal.transport.coap.CoapTransportLayer;
import com.agh.fastmachine.server.internal.transport.operations.ReadOperations;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoapReadOperations extends ReadOperations<CoapTransportLayer> {
    private static final Logger LOG = LoggerFactory.getLogger(CoapReadOperations.class);
    private CoapServer coapServer;
    private int port;

    public CoapReadOperations(ClientProxyImpl clientProxy) {
        super(clientProxy);
        coapServer = transportLayer.getCoapServer();
        port = transportLayer.getPort();
    }

    @Override
    public <T extends ObjectInstanceProxy> void read(ObjectBaseProxy<T> object) {
        String endpoint = getEndpoint(object);
        CoapResponse response = doGetTlvAndGetResponse(endpoint);
        handleFailures(response, endpoint);

        if (response.isSuccess()) {
            ObjectBaseProxy newValue = readParser.deserialize(object, response.getPayload());
            object.internal().update(newValue);
            LOG.debug("Read object: {}", endpoint);
        }
    }

    @Override
    public void read(ObjectInstanceProxy instance) {
        String endpoint = getEndpoint(instance);
        CoapResponse response = doGetTlvAndGetResponse(endpoint);
        handleFailures(response, endpoint);

        if (response.isSuccess()) {
            ObjectInstanceProxy newValue = readParser.deserialize(instance, response.getPayload());
            instance.internal().update(newValue);
            LOG.debug("Read instance: {}", endpoint);
        }
    }

    @Override
    public void read(ObjectResourceProxy<?> resource) {
        String endpoint = getEndpoint(resource);
        CoapClient coapClient = new CoapClient(endpoint);
        coapClient.setEndpoint(coapServer.getEndpoint(port));

        if (OpaqueResourceValue.class.equals(resource.getValueType())) {
            CoapResponse response = coapClient.get(MediaTypeRegistry.OPAQUE.getKey());
            handleFailures(response, endpoint);
            OpaqueResourceValue newValue = new OpaqueResourceValue(response.getPayload());
            resource.internal().update(newValue);
        } else if (resource instanceof ObjectMultipleResourceProxy) {
            CoapResponse response = coapClient.get(MediaTypeRegistry.TLV.getKey());
            handleFailures(response, endpoint);
            ObjectMultipleResourceProxy<?> newValue = (ObjectMultipleResourceProxy<?>) readParser.deserialize(resource, response.getPayload());
            resource.internal().update(newValue);
        } else {
            CoapResponse response = coapClient.get(MediaTypeRegistry.PLAIN_TEXT.getKey());
            handleFailures(response, endpoint);
            ResourceValue<?> newValue = readParser.deserialize(resource.getValue(), response.getPayload());
            resource.internal().update(newValue);
        }
    }

    private CoapResponse doGetTlvAndGetResponse(String endpoint) {
        CoapClient coapClient = new CoapClient(endpoint);
        coapClient.setEndpoint(coapServer.getEndpoint(port));
        return coapClient.get(MediaTypeRegistry.TLV.getKey());
    }

    private void handleFailures(CoapResponse response, String requestEndpoint) {
        if (!response.isSuccess()) {
            if (response.getCode() == CoAP.ResponseCode.NOT_FOUND) {
                LOG.error("Read failed. Node {} does not exist on client {}.", requestEndpoint, clientProxy.getClientEndpointName());
                throw new RuntimeException("4.04 Not found: " + requestEndpoint);
            }
            LOG.error("Failed to read {} on client {}. Respose code: {}", requestEndpoint, clientProxy.getClientEndpointName(), response.getCode());
            throw new RuntimeException("Nie uDaLo sieeee");
        }
    }

}
