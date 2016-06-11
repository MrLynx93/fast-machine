package com.agh.fastmachine.server.internal.transport.coap.operations;

import com.agh.fastmachine.core.internal.parser.WriteParser;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.internal.transport.coap.CoapTransportLayer;
import com.agh.fastmachine.server.internal.transport.operations.CreateOperations;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoapCreateOperations extends CreateOperations<CoapTransportLayer> {
    private static final Logger LOG = LoggerFactory.getLogger(CoapCreateOperations.class);
    private CoapServer coapServer;
    private Integer port;

    public CoapCreateOperations(ClientProxyImpl clientProxy) {
        super(clientProxy);
        coapServer = transportLayer.getCoapServer();
        port = transportLayer.getPort();
    }

    @Override
    public <T extends ObjectInstanceProxy> void create(T instance) {
        String endpoint = getEndpoint(instance); // todo override getEndpoint?
        CoapResponse response = doPostAndGetResponse(instance, endpoint);
        int createdInstanceId = Integer.parseInt(response.getOptions().getLocationPath().get(1));
        handleFailures(response, endpoint);

        if (response.isSuccess()) {
            LOG.debug("Created object instance: {}", endpoint);
            server.internal().getObjectTreeCreator().connectToRemoteClient(instance, createdInstanceId, clientProxy);
        }
    }

    @Override
    public <T extends ObjectInstanceProxy> void create(T instance, int instanceId) {
        instance.internal().setId(instanceId);
        String endpoint = getEndpoint(instance);
        CoapResponse response = doPostAndGetResponse(instance, endpoint);
        handleFailures(response, endpoint);

        if (response.isSuccess()) {
            LOG.debug("Created object instance: {}", endpoint);
            server.internal().getObjectTreeCreator().connectToRemoteClient(instance, instanceId, clientProxy);
        }
    }

    private <T extends ObjectInstanceProxy> CoapResponse doPostAndGetResponse(T instance, String endpoint) {
        CoapClient coapClient = new CoapClient(endpoint);
        coapClient.setEndpoint(coapServer.getEndpoint(port));
        return coapClient.post(WriteParser.serialize(instance), MediaTypeRegistry.APPLICATION_LINK_FORMAT);
    }

    private void handleFailures(CoapResponse response, String requestEndpoint) {
        if (!response.isSuccess()) {
            LOG.error("Failed to create object {} on client {}. Response code: {}", requestEndpoint, clientProxy.getClientEndpointName(), response.getCode());
        }
    }
}
