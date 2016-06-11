package com.agh.fastmachine.server.api;

import com.agh.fastmachine.server.internal.ObjectTreeCreator;
import com.agh.fastmachine.server.internal.client.ClientManager;
import com.agh.fastmachine.server.internal.service.BootstrapService;
import com.agh.fastmachine.server.internal.service.RegistrationService;
import com.agh.fastmachine.server.api.listener.RegistrationListener;
import com.agh.fastmachine.server.internal.transport.coap.CoapTransportLayer;
import com.agh.fastmachine.server.internal.transport.TransportLayer;
import com.agh.fastmachine.server.internal.transport.coap.CoapTransportConfiguration;
import com.agh.fastmachine.server.internal.transport.TransportConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Server {
    private static final Logger LOG = LoggerFactory.getLogger(Server.class);
    private ObjectTreeCreator objectTreeCreator = new ObjectTreeCreator();
    private ClientManager clientManager = new ClientManager(this);
    private BootstrapService bootstrapService;
    private RegistrationService registrationService;
    private TransportLayer transportLayer;
    private ServerConfiguration configuration;
    private Internal internal = new Internal();

    public void start() {
        bootstrapService = new BootstrapService(this);
        registrationService = new RegistrationService(this);

        TransportConfiguration transportConfiguration = null;
        if (configuration.getTransport() == ServerConfiguration.TRASPORT_COAP) {
            transportLayer = new CoapTransportLayer();
            transportConfiguration = createCoapTransportConfiguration();
        }
        transportLayer.start(transportConfiguration);
    }

    public void stop() {
        transportLayer.stop();
    }

    public void setConfiguration(ServerConfiguration configuration) {
        this.configuration = configuration;
    }

    public ServerConfiguration getConfiguration() {
        return configuration;
    }

    public Map<String, ClientProxy> getClients() {
        return (Map<String, ClientProxy>) clientManager.getClients();
    }

    public ClientProxy createClient(String endpointClientName) {
        return clientManager.createClient(endpointClientName);
    }

    public ClientProxy getClientForEndpointName(String clientEndpointName) {
        return clientManager.getClientForEndpointName(clientEndpointName);
    }

    public void setRegistrationListener(RegistrationListener listener) {
        clientManager.setRegistrationListener(listener);
    }

    private CoapTransportConfiguration createCoapTransportConfiguration() {
        CoapTransportConfiguration coapConfiguration = new CoapTransportConfiguration();
        coapConfiguration.setPort(configuration.getPort());
        coapConfiguration.setServer(this);
        return coapConfiguration;
    }

    ////// INTERNAL //////

    public Internal internal() {
        return internal;
    }

    public class Internal {
        public RegistrationService getRegistrationService() {
            return registrationService;
        }

        public BootstrapService getBootstrapService() {
            return bootstrapService;
        }

        public TransportLayer getTransportLayer() {
            return transportLayer;
        }

        public ObjectTreeCreator getObjectTreeCreator() {
            return objectTreeCreator;
        }

        public ClientManager getClientManager() {
            return clientManager;
        }
    }

}
