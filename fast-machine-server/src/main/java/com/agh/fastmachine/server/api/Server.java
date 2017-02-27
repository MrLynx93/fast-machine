package com.agh.fastmachine.server.api;

import com.agh.fastmachine.server.internal.ObjectTreeCreator;
import com.agh.fastmachine.server.internal.client.ClientManager;
import com.agh.fastmachine.server.internal.service.BootstrapService;
import com.agh.fastmachine.server.internal.service.RegistrationService;
import com.agh.fastmachine.server.api.listener.RegistrationListener;
import com.agh.fastmachine.server.internal.transport.coap.CoapTransport;
import com.agh.fastmachine.server.internal.transport.Transport;
import com.agh.fastmachine.server.internal.transport.coap.CoapConfiguration;
import com.agh.fastmachine.server.internal.transport.TransportConfiguration;
import com.agh.fastmachine.server.internal.transport.mqtt.MqttConfiguration;
import com.agh.fastmachine.server.internal.transport.mqtt.MqttTransport;
import com.agh.fastmachine.server.internal.transport.stats.Stats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Server {
    private static final Logger LOG = LoggerFactory.getLogger(Server.class);
    private ObjectTreeCreator objectTreeCreator = new ObjectTreeCreator();
    private ClientManager clientManager = new ClientManager(this);
    private BootstrapService bootstrapService;
    private RegistrationService registrationService;
    private Transport transport;
    private TransportConfiguration transportConfiguration;
    private ServerConfiguration configuration;
    private Internal internal = new Internal();
    private String name;

    public Server(ServerConfiguration serverConf, TransportConfiguration transportConf) {
        this.configuration = serverConf;
        this.name = serverConf.getName();
        this.transportConfiguration = transportConf;
    }

    public Stats getStats() {
        return transport.stats;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void start() {
        bootstrapService = new BootstrapService(this);
        registrationService = new RegistrationService(this);

        if (configuration.getTransport() == ServerConfiguration.TRASPORT_COAP) {
            transport = new CoapTransport();
            if (transportConfiguration == null) {
                transportConfiguration = createCoapTransportConfiguration();
            }
            transportConfiguration.setServer(this);
        } else {
            transport = new MqttTransport();
            ((MqttConfiguration)transportConfiguration).setServerName(name);
            transportConfiguration.setServer(this);
        }
        transport.start(transportConfiguration);
    }

    public void stop() {
        transport.stop();
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

    private CoapConfiguration createCoapTransportConfiguration() {
        CoapConfiguration coapConfiguration = new CoapConfiguration();
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

        public Transport getTransportLayer() {
            return transport;
        }

        public ObjectTreeCreator getObjectTreeCreator() {
            return objectTreeCreator;
        }

        public ClientManager getClientManager() {
            return clientManager;
        }
    }

}
