package com.agh.fastmachine.server.internal.transport.coap;

import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.internal.parser.RegistrationInfoParser;
import com.agh.fastmachine.server.internal.service.registrationinfo.RegistrationInfo;
import com.agh.fastmachine.server.internal.transport.LWM2M;
import com.agh.fastmachine.server.internal.transport.Lwm2mRequest;
import com.agh.fastmachine.server.internal.transport.Lwm2mResponse;
import com.agh.fastmachine.server.internal.transport.Transport;
import com.agh.fastmachine.server.internal.transport.coap.message.CoapRequestBuilder;
import com.agh.fastmachine.server.internal.transport.coap.message.Lwm2mCoapRequest;
import com.agh.fastmachine.server.internal.transport.coap.message.Lwm2mCoapResponse;
import com.agh.fastmachine.server.internal.transport.coap.resource.CoapBootstrapResource;
import com.agh.fastmachine.server.internal.transport.coap.resource.CoapRegistrationResource;
import com.agh.fastmachine.server.internal.transport.stats.Event;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.MessageObserverAdapter;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.server.resources.Resource;
import org.eclipse.californium.scandium.DTLSConnector;
import org.eclipse.californium.scandium.config.DtlsConnectorConfig;
import org.eclipse.californium.scandium.dtls.cipher.CipherSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.List;

public class CoapTransport extends Transport<CoapConfiguration, Lwm2mCoapRequest> {
    private static final Logger LOG = LoggerFactory.getLogger(CoapTransport.class);
    private RegistrationInfoParser registrationInfoParser = new RegistrationInfoParser();
    private CoapServer coapServer;
    private Endpoint endpoint;

    public CoapTransport() {
        requestBuilder = new CoapRequestBuilder();
    }

    @Override
    public void start(CoapConfiguration configuration) {
        this.configuration = configuration;
        Resource bootstrapResource = new CoapBootstrapResource(configuration.getServer(), stats);
        Resource registrationResource = new CoapRegistrationResource(configuration.getServer(), stats);

        coapServer = new CoapServer(configuration.getPort()).add(bootstrapResource, registrationResource);

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
                coapServer = new CoapServer().add(bootstrapResource, registrationResource);
                coapServer.addEndpoint(new CoapEndpoint(connector, NetworkConfig.getStandard()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // TODO should this be another endpoint?
        endpoint = coapServer.getEndpoint(configuration.getPort());
        coapServer.start();
        LOG.info("Started LWM2M server on port {}", configuration.getPort());
    }

    @Override
    public void stop() {
        coapServer.stop();
    }

    @Override // TODO TIMEOUT
    protected void doSendRequest(ClientProxyImpl client, Lwm2mCoapRequest request) throws Exception {
        Request coapRequest = request.toCoapRequest();
        coapRequest.setURI(client.getClientUrl() + request.getCoapPath());
        coapRequest.addMessageObserver(new RequestCacheMessageObserver(request));
        endpoint.sendRequest(coapRequest);

        stats.addEvent(client, Event.downlinkRequestSendSuccess(request.getOperation()));
    }

    @Override
    protected boolean isNotify(Lwm2mRequest request, Lwm2mResponse response) {
        return (request.getOperation() == LWM2M.Operation.I_OBSERVE && response.isSuccess());
    }

    private class RequestCacheMessageObserver extends MessageObserverAdapter {
        private final Lwm2mCoapRequest request;

        RequestCacheMessageObserver(Lwm2mCoapRequest request) {
            this.request = request;
        }

        @Override
        public void onResponse(Response coapResponse) {
            coapResponse.setToken(request.getToken().getBytes());
            Lwm2mResponse response = Lwm2mCoapResponse.fromCoapResponse(coapResponse);
            handleResponse(response);
        }

    }

    public RegistrationInfo parseRegistrationInfo(Request request) {
        List<String> params = request.getOptions().getUriQuery();
        String payload = request.getPayloadString();
        return registrationInfoParser.parseRegistrationInfo(payload, params);
    }

}
