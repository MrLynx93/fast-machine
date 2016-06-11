package com.agh.fastmachine.server.internal.transport.coap;

import com.agh.fastmachine.server.api.ClientProxy;
import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.internal.parser.RegistrationInfoParser;
import com.agh.fastmachine.server.internal.transport.coap.operations.*;
import com.agh.fastmachine.server.internal.transport.coap.resource.CoapBootstrapResource;
import com.agh.fastmachine.server.internal.transport.coap.resource.CoapRegistrationResource;
import com.agh.fastmachine.server.internal.service.registrationinfo.RegistrationInfo;
import com.agh.fastmachine.server.internal.transport.TransportLayer;
import com.agh.fastmachine.server.internal.transport.operations.*;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.server.resources.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CoapTransportLayer extends TransportLayer<CoapTransportConfiguration> {
    private static final Logger LOG = LoggerFactory.getLogger(CoapTransportLayer.class);
    private RegistrationInfoParser registrationInfoParser = new RegistrationInfoParser();
    private CoapServer coapServer;

    @Override
    public void start(CoapTransportConfiguration configuration) {
        this.configuration = configuration;
        int port = configuration.getPort();
        Resource bootstrapResource = new CoapBootstrapResource(configuration.getServer());
        Resource registrationResource = new CoapRegistrationResource(configuration.getServer());

        coapServer = new CoapServer(port).add(bootstrapResource, registrationResource);
        coapServer.start();
        LOG.info("Started LWM2M server on port {}", port);
    }

    @Override
    public void stop() {
        coapServer.stop();
    }

    @Override
    public ObserveOperations observeOperations(ClientProxy clientProxy) {
        return new CoapObserveOperations((ClientProxyImpl) clientProxy);
    }

    @Override
    public CreateOperations createOperations(ClientProxy clientProxy) {
        return new CoapCreateOperations((ClientProxyImpl) clientProxy);
    }

    @Override
    public ReadOperations readOperations(ClientProxy clientProxy) {
        return new CoapReadOperations((ClientProxyImpl) clientProxy);
    }

    @Override
    public WriteOperations writeOperations(ClientProxy clientProxy) {
        return new CoapWriteOperations((ClientProxyImpl) clientProxy);
    }

    @Override
    public DiscoverOperations discoverOperations(ClientProxy clientProxy) {
        return new CoapDiscoverOperations((ClientProxyImpl) clientProxy);
    }

    @Override
    public WriteAttributeOperations writeAttributeOperations(ClientProxy clientProxy) {
        return new CoapWriteAttributeOperations((ClientProxyImpl) clientProxy);
    }

    @Override
    public DeleteOperations deleteOperations(ClientProxy clientProxy) {
        return new CoapDeleteOperations((ClientProxyImpl) clientProxy);
    }

    @Override
    public ExecuteOperations executeOperations(ClientProxy clientProxy) {
        return new CoapExecuteOperations((ClientProxyImpl) clientProxy);
    }

    public RegistrationInfo parseRegistrationInfo(Request request) {
        List<String> params = request.getOptions().getUriQuery();
        String payload = request.getPayloadString();
        return registrationInfoParser.parseRegistrationInfo(payload, params);
    }

    public CoapServer getCoapServer() {
        return coapServer;
    }

    public int getPort() {
        return configuration.getPort();
    }
}
