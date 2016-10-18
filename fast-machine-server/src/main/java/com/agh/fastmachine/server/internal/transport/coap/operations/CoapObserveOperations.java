package com.agh.fastmachine.server.internal.transport.coap.operations;

import com.agh.fastmachine.core.api.model.resourcevalue.OpaqueResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.ResourceValue;
import com.agh.fastmachine.server.api.model.ObjectNodeProxy;
import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;
import com.agh.fastmachine.server.api.listener.ObservationListener;
import com.agh.fastmachine.server.internal.transport.coap.CoapTransport;
import com.agh.fastmachine.server.internal.transport.operations.ObserveOperations;
import org.eclipse.californium.core.*;
import org.eclipse.californium.core.coap.CoAP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class CoapObserveOperations extends ObserveOperations<CoapTransport> {
    private static final Logger LOG = LoggerFactory.getLogger(CoapObserveOperations.class);
    private final HashMap<ObjectNodeProxy, CoapObserveRelation> observeRelations = new HashMap<>();
    private CoapServer coapServer;
    private int port;

    public CoapObserveOperations(ClientProxyImpl clientProxy) {
        super(clientProxy);
        coapServer = transportLayer.getCoapServer();
        port = transportLayer.getPort();
    }

    @Override
    public <T extends ObjectInstanceProxy> void observe(ObjectBaseProxy<T> object, ObservationListener<ObjectBaseProxy<T>> listener) {
        String endpoint = getEndpoint(object);
        CoapClient coapClient = new CoapClient(endpoint);
        coapClient.setEndpoint(coapServer.getEndpoint(port));
        observeRelations.put(object, coapClient.observe(new ObservationHandler<>(object, listener)));
        LOG.debug("Started observing object: {}", endpoint);
    }

    @Override
    public void observe(ObjectInstanceProxy instance, ObservationListener<ObjectInstanceProxy> listener) {
        String endpoint = getEndpoint(instance);
        CoapClient coapClient = new CoapClient(endpoint);
        coapClient.setEndpoint(coapServer.getEndpoint(port));
        observeRelations.put(instance, coapClient.observe(new ObservationHandler<>(instance, listener)));
        LOG.debug("Started observing instance: {}", endpoint, clientProxy.getClientEndpointName());
    }

    @Override
    public <T extends ResourceValue<?>> void observe(ObjectResourceProxy<T> resource, ObservationListener<ObjectResourceProxy<T>> listener) {
        String endpoint = getEndpoint(resource);
        CoapClient coapClient = new CoapClient(endpoint);
        coapClient.setEndpoint(coapServer.getEndpoint(port));
        observeRelations.put(resource, coapClient.observeAndWait(new ObservationHandler<>(resource, listener)));
        LOG.debug("Started observing resource: {}", endpoint, clientProxy.getClientEndpointName());
    }

    @Override
    public void cancelObservation(ObjectBaseProxy object) {
        cancelNodeObservation(object);
        LOG.debug("Stopped observing object: {}", getEndpoint(object));
    }

    @Override
    public void cancelObservation(ObjectInstanceProxy instance) {
        cancelNodeObservation(instance);
        LOG.debug("Stopped observing instance: {}", getEndpoint(instance));
    }

    @Override
    public void cancelObservation(ObjectResourceProxy resource) {
        cancelNodeObservation(resource);
        LOG.debug("Stopped observing resource: {}", getEndpoint(resource));
    }

    private void cancelNodeObservation(ObjectNodeProxy node) {
        observeRelations.remove(node);
        node.getAttributes().cancel = true;
        node.writeAttributes();
    }

    private class ObservationHandler<T extends ObjectNodeProxy> implements CoapHandler {
        private ObservationListener<T> listener;
        private T node;

        public ObservationHandler(T node, ObservationListener<T> listener) {
            this.listener = listener;
            this.node = node;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onLoad(CoapResponse response) {
            if (!isNotification(response)) {
                return;
            }

            if (node instanceof ObjectResourceProxy) {
                ObjectResourceProxy<?> resource = (ObjectResourceProxy) node;
                if (OpaqueResourceValue.class.equals(resource.getValueType())) {
                    OpaqueResourceValue newValue = new OpaqueResourceValue(response.getPayload());
                    resource.internal().update(newValue);
                } else {
                    ResourceValue<?> newValue = readParser.deserialize(resource.getValue(), response.getPayload());
                    resource.internal().update(newValue);
                }
            }
            if (node instanceof ObjectInstanceProxy) {
                ObjectInstanceProxy instance = (ObjectInstanceProxy) node;
                ObjectInstanceProxy newValue = readParser.deserialize(instance, response.getPayload());
                instance.internal().update(newValue);
                listener.onNotify(node);
                return;
            }
            if (node instanceof ObjectBaseProxy) {
                ObjectBaseProxy<?> object = (ObjectBaseProxy) node;
                ObjectBaseProxy newValue = readParser.deserialize(object, response.getPayload());
                object.internal().update(newValue);
            }
            listener.onNotify(node);
            // TODO implement CancelObservation by sending Cancel Observation as the response of Notify
            // TODO should there be a method cancelOnNextNotify() which sets the flag?
        }

        private boolean isNotification(CoapResponse response) {
            return response.getCode() != CoAP.ResponseCode.CONTENT;
        }

        @Override
        public void onError() {
            LOG.error("Error inside observe listener");
        }

    }
}
