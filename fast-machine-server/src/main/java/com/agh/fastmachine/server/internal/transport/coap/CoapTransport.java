package com.agh.fastmachine.server.internal.transport.coap;

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
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.server.resources.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        int port = configuration.getPort();
        Resource bootstrapResource = new CoapBootstrapResource(configuration.getServer());
        Resource registrationResource = new CoapRegistrationResource(configuration.getServer());

        coapServer = new CoapServer(port).add(bootstrapResource, registrationResource);
        endpoint = coapServer.getEndpoint(configuration.getPort());
        coapServer.start();
        LOG.info("Started LWM2M server on port {}", port);
    }

    @Override
    public void stop() {
        coapServer.stop();
    }

    @Override
    protected void doSendRequest(Lwm2mCoapRequest request) {
        CoapClient coapClient = new CoapClient(request.getCoapPath());
        coapClient.setEndpoint(endpoint);
        coapClient.advanced(handler, request.toCoapRequest());
    }

    @Override
    protected boolean isNotify(Lwm2mRequest request, Lwm2mResponse response) {
        return (request.getOperation() == LWM2M.Operation.I_OBSERVE && response.isSuccess());
    }

    private CoapHandler handler = new CoapHandler() {

        @Override
        public void onLoad(CoapResponse coapResponse) {
            Lwm2mResponse response = Lwm2mCoapResponse.fromCoapResponse(coapResponse);
            handleResponse(response);
        }

        @Override
        public void onError() {
        }

    };

    public RegistrationInfo parseRegistrationInfo(Request request) {
        List<String> params = request.getOptions().getUriQuery();
        String payload = request.getPayloadString();
        return registrationInfoParser.parseRegistrationInfo(payload, params);
    }

}
