package com.agh.fastmachine.server.internal.transport.coap;

import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.internal.parser.RegistrationInfoParser;
import com.agh.fastmachine.server.internal.service.registrationinfo.RegistrationInfo;
import com.agh.fastmachine.server.internal.transport.*;
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
    private CoapOperations operations = new CoapOperations();
    private CoapServer coapServer;
    private Endpoint endpoint;

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
        CoapClient coapClient = new CoapClient(request.getPath().toString());
        coapClient.setEndpoint(endpoint);
        coapClient.advanced(handler, request.toCoapRequest());
    }

    private CoapHandler handler = new CoapHandler() {

        @Override
        public void onLoad(CoapResponse coapResponse) {
            Lwm2mResponse response = Lwm2mCoapResponse.fromCoapResponse(coapResponse);
            String token = response.getToken();
            PendingRequest pendingRequest = pendingRequests.get(token);
            Lwm2mRequest request = pendingRequest.getRequest();

            if (isNotify(request, response)) {
                observeHandlers.get(token).onNotify(response);
            }
            if (isCancelObserve(request, response)) {
                observeHandlers.remove(token);
            }
            pendingRequest.complete(response);
        }

        @Override
        public void onError() {
        }

        private boolean isNotify(Lwm2mRequest request, Lwm2mResponse response) {
            return (request.getOperation() == LWM2M.Operation.I_NOTIFY && response.isSuccess());
        }

        private boolean isCancelObserve(Lwm2mRequest request, Lwm2mResponse response) {
            return (request.getOperation() == LWM2M.Operation.I_CANCEL_OBSERVATION && response.isSuccess());
        }
    };

    public RegistrationInfo parseRegistrationInfo(Request request) {
        List<String> params = request.getOptions().getUriQuery();
        String payload = request.getPayloadString();
        return registrationInfoParser.parseRegistrationInfo(payload, params);
    }

}
