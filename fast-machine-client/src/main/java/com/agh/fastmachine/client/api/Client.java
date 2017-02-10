package com.agh.fastmachine.client.api;

import com.agh.fastmachine.client.internal.ClientCoapResource;
import com.agh.fastmachine.client.internal.ServerObjectResolver;
import com.agh.fastmachine.client.api.model.ObjectBase;
import com.agh.fastmachine.client.api.model.ObjectInitializer;
import com.agh.fastmachine.client.api.model.builtin.AccessControlObjectBase;
import com.agh.fastmachine.client.api.model.builtin.SecurityObjectInstance;
import com.agh.fastmachine.client.api.model.builtin.ServerObjectInstance;
import com.agh.fastmachine.client.internal.service.observation.ObservationInterface;
import com.agh.fastmachine.client.internal.service.registration.RegistrationInterface;
import com.agh.fastmachine.client.internal.visitor.DiscoverVisitor;
import com.agh.fastmachine.client.internal.parser.ClientReadParser;
import com.agh.fastmachine.core.internal.parser.ReadParser;
import com.agh.fastmachine.core.internal.parser.WriteParser;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.server.ServerMessageDeliverer;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.cipher.CipherSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Client {
    private static final Logger LOG = LoggerFactory.getLogger(Client.class);

    private String rootPath = "";
    private Map<Integer, ObjectBase<?>> objectBaseMap = new HashMap<>();
    private Map<Integer, SecurityObjectInstance> serverSecurityMap = new HashMap<>();

    private ServerObjectResolver serverObjectResolver = new ServerObjectResolver();
    private RegistrationInterface registrationInterface = new RegistrationInterface(this);
    private ObservationInterface observationInterface = new ObservationInterface(serverObjectResolver);
    private ObjectInitializer objectInitializer;

    private String endpointClientName;

    private ClientCoapResource clientResource;
    private CoapServer coapServer;
    private Endpoint endpoint;


    public Client(String name, List<ObjectBase<?>> objects, CoapClientConf configuration) {
        this.endpointClientName = name;
        this.endpoint = prepareEndpoint(configuration);
        this.coapServer = new CoapServer();
        this.coapServer.addEndpoint(this.endpoint);
//        endpoint = coapServer.getEndpoint(configuration.getPort());
        this.clientResource = new ClientCoapResource(rootPath);
        this.coapServer.setMessageDeliverer(new ServerMessageDeliverer(clientResource));
        setObjects(objects);
    }

    private Endpoint prepareEndpoint(CoapClientConf configuration) {
        if (configuration == null) {
            return new CoapEndpoint(5685);
        }
        if (configuration.isDtls()) {
            try {
                KeyStore keyStore = KeyStore.getInstance("JKS");
                InputStream inKeyStore = getClass().getClassLoader().getResourceAsStream(configuration.getKeyStoreLocation());
                keyStore.load(inKeyStore, configuration.getKeyStorePassword().toCharArray());

                DtlsConnectorConfig.Builder connConfig = new DtlsConnectorConfig.Builder(new InetSocketAddress(configuration.getPort()));
                connConfig.setSupportedCipherSuites(new CipherSuite[]{CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CCM_8});
                connConfig.setIdentity((PrivateKey) keyStore.getKey("server", configuration.getKeyStorePassword().toCharArray()), keyStore.getCertificateChain("server"), true);
                connConfig.setClientAuthenticationRequired(false);

                DTLSConnector connector = new DTLSConnector(connConfig.build(), null);
                return new CoapEndpoint(connector, NetworkConfig.getStandard());
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        } else {
            return new CoapEndpoint(configuration.getPort());
        }
    }

    public void requestBootstrap() throws InterruptedException {
        SecurityObjectInstance bootstrapSecurity = null;
        for (SecurityObjectInstance security : serverSecurityMap.values()) {
            if (security.bootstrapServer.getValue().value) {
                bootstrapSecurity = security;
                break;
            }
        }
        if (bootstrapSecurity == null) {
            LOG.error("Client initiated bootstrap cannot be performed");
            return;
            //TODO throw exception, client initiated bootstrap cannot be performed
            //TODO client initiated bootstrap is only possible having BS Account, but in reality it is not necessary, while security model is
        }

        Request bootstrap = new Request(CoAP.Code.POST);
        bootstrap.setURI(bootstrapSecurity.serverUri + "/bs?ep=" + endpointClientName);
        bootstrap.send();
    }

    public void deregister(int serverInstanceId) {
        ServerObjectInstance serverInstance = (ServerObjectInstance) objectBaseMap.get(1).getObjectInstance(serverInstanceId);
        registrationInterface.deregister(serverInstance);
        LOG.debug("Deregistered from server shortServerId[{}]", serverInstance.shortServerId.getValue().value);
    }

    public void start() {
        coapServer.start();
        Map<Integer, ServerObjectInstance> servers = ((ObjectBase<ServerObjectInstance>) objectBaseMap.get(1)).getObjectInstances();
        if (servers.size() == 0) {
            //TODO start bootstrap procedure
        } else {
            for (ServerObjectInstance server : servers.values()) {
                registrationInterface.registerIntoServer(server);
            }
        }

        StringBuilder serverUriList = new StringBuilder("");
        Map<Integer, SecurityObjectInstance> securityList = (Map<Integer, SecurityObjectInstance>) objectBaseMap.get(0).getObjectInstances();
        for (SecurityObjectInstance securityObjectInstance : securityList.values()) {
            serverUriList.append(securityObjectInstance.serverUri.getValue().value).append(",");
        }
        LOG.debug("Started client and connected to servers: [{}]", serverUriList.toString());
        LOG.debug("EndpointClientName: {}", endpointClientName);
    }

    public void stop() {
        Map<Integer, ServerObjectInstance> servers = ((ObjectBase<ServerObjectInstance>) objectBaseMap.get(1)).getObjectInstances();
        for (Integer serverInstanceId : servers.keySet()) {
            deregister(serverInstanceId);
        }
        coapServer.stop();
    }

    private void setObjects(List<ObjectBase<?>> objects) {
        AccessControlObjectBase acob = new AccessControlObjectBase();
        objectBaseMap.put(acob.getId(), acob);

        objectInitializer = new ObjectInitializer(this);
        objectInitializer.initializeObject(acob);

        for (ObjectBase toAdd : objects) {
            if (objectBaseMap.containsKey(toAdd.getId())) {
                return;//TODO throw exception, invalid objects+
            }
            objectInitializer.initializeObject(toAdd);
            objectBaseMap.put(toAdd.getId(), toAdd);
        }
        LOG.debug("Initialized LWM2M objects");
    }

    public ObservationInterface getObservationInterface() {
        return observationInterface;
    }

    public String getEndpointClientName() {
        return endpointClientName;
    }

    public String getRootPath() {
        return rootPath;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public Map<Integer, ObjectBase<?>> getObjectBaseMap() {
        return Collections.unmodifiableMap(objectBaseMap);
    }

    public Map<Integer, SecurityObjectInstance> getServerSecurityMap() {
        return serverSecurityMap;
    }

    public ServerObjectResolver getServerObjectResolver() {
        return serverObjectResolver;
    }

    public ObjectInitializer getObjectInitializer() {
        return objectInitializer;
    }

    public RegistrationInterface getRegistrationInterface() {
        return registrationInterface;
    }

    public ClientCoapResource getClientResource() {
        return clientResource;
    }

}

