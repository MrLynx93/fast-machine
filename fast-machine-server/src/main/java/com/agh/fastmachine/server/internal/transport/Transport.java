package com.agh.fastmachine.server.internal.transport;

import com.agh.fastmachine.core.api.model.Attributes;
import com.agh.fastmachine.core.api.model.resourcevalue.OpaqueResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.ResourceValue;
import com.agh.fastmachine.core.internal.parser.ReadParser;
import com.agh.fastmachine.core.internal.parser.WriteAttributesParser;
import com.agh.fastmachine.server.api.listener.ObservationListener;
import com.agh.fastmachine.server.api.model.*;
import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.internal.parser.ServerReadParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Transport<T extends TransportConfiguration, REQ extends Lwm2mRequest> {
    private static final Logger LOG = LoggerFactory.getLogger(Transport.class);
    protected final Map<String, PendingRequest> pendingRequests = new ConcurrentHashMap<>();
    protected final Map<String, ObserveHandler> observeHandlers = new ConcurrentHashMap<>();
    private final WriteAttributesParser writeAttributesParser = new WriteAttributesParser();
    private final ReadParser readParser = new ServerReadParser();
    protected RequestBuilder<REQ> requestBuilder;
    protected T configuration;

    public abstract void start(T configuration);
    public abstract void stop();

    protected abstract void doSendRequest(REQ request);

    public PendingRequest sendRequest(REQ request) {
        PendingRequest pendingRequest = new PendingRequest(request);
        pendingRequests.put(request.getToken(), pendingRequest);
        doSendRequest(request);
        return pendingRequest;
    }

    protected void handleResponse(Lwm2mResponse response) {
        String token = response.getToken();
        PendingRequest pendingRequest = pendingRequests.get(token);
        Lwm2mRequest request = pendingRequest.getRequest();

        if (isNotify(request, response) && pendingRequest.isCompleted()) {
            observeHandlers.get(token).onNotify(response);
        }
        if (isCancelObserve(request, response)) {
            observeHandlers.remove(token);
        }
        pendingRequest.complete(response);
    }

    protected abstract boolean isNotify(Lwm2mRequest request, Lwm2mResponse response);

    public void startObserve(String token, ObserveHandler observeHandler) {
        observeHandlers.put(token, observeHandler);
    }

    public void stopObserve(String token) {
        observeHandlers.remove(token);
    }

    /********************  CREATE  ********************/

    public void create(ObjectInstanceProxy instance) {
        REQ request = requestBuilder.buildCreateRequest(instance);
        PendingRequest pendingRequest = sendRequest(request);

        Lwm2mResponse response = pendingRequest.waitForCompletion();
        if (response.isSuccess()) {
            instance.internal().setId(response.getCreatedInstanceId());
            connectToRemote(instance);
            LOG.debug("Created object instance: {}", instance.getPath());
        }
    }

    public void create(ObjectInstanceProxy instance, int instanceId) {
        REQ request = requestBuilder.buildCreateRequest(instance, instanceId);
        PendingRequest pendingRequest = sendRequest(request);

        Lwm2mResponse response = pendingRequest.waitForCompletion();
        if (response.isSuccess()) {
            instance.internal().setId(response.getCreatedInstanceId());
            connectToRemote(instance);
            LOG.debug("Created object instance: {}", instance.getPath());
        }
    }

    /********************  DELETE  ********************/

    public void delete(ObjectInstanceProxy instance) {
        REQ request = requestBuilder.buildDeleteRequest(instance);
        PendingRequest pendingRequest = sendRequest(request);

        Lwm2mResponse response = pendingRequest.waitForCompletion();
        if (response.isSuccess()) {
            LOG.debug("Deleted instance: {}", instance.getPath());
        }
    }

    /********************  DISCOVER  ********************/

    public void discover(ObjectBaseProxy<?> object) {
        REQ request = requestBuilder.buildDiscoverRequest(object);
        PendingRequest pendingRequest = sendRequest(request);

        Lwm2mResponse response = pendingRequest.waitForCompletion();
        if (response.isSuccess()) {
            Attributes attributes = writeAttributesParser.parseWriteAttributes(response.getPayloadText());
            if (attributes != null) {
                object.internal().updateAttributes(attributes);
            }
            List<Integer> supportedResources = writeAttributesParser.parseSupportedResources(response.getPayloadText());
            for (ObjectInstanceProxy instance : object.getObjectInstances().values()) {
                for (Map.Entry<Integer, ? extends ObjectResourceProxy<?>> resourceEntry : instance.getResources().entrySet()) {
                    ObjectResourceProxy<?> resource = resourceEntry.getValue();
                    resource.internal().setSupported(supportedResources.contains(resourceEntry.getKey()));
                }
            }
            LOG.debug("Discovered object: {}", object.getPath());
            LOG.debug("Discover message: {}", response.getPayloadText());
        }
    }

    public void discover(ObjectInstanceProxy instance) {
        REQ request = requestBuilder.buildDiscoverRequest(instance);
        PendingRequest pendingRequest = sendRequest(request);

        Lwm2mResponse response = pendingRequest.waitForCompletion();
        if (response.isSuccess()) {
            Attributes attributes = writeAttributesParser.parseWriteAttributes(response.getPayloadText());
            if (attributes != null) {
                instance.internal().updateAttributes(attributes);
            }
            LOG.debug("Discovered instance: {}", instance.getPath());
            LOG.debug("Discover message: {}", response.getPayloadText());
        }
    }

    public void discover(ObjectResourceProxy<?> resource) {
        REQ request = requestBuilder.buildDiscoverRequest(resource);
        PendingRequest pendingRequest = sendRequest(request);

        Lwm2mResponse response = pendingRequest.waitForCompletion();
        if (response.isSuccess()) {
            Attributes attributes = writeAttributesParser.parseResourceWriteAttributes(response.getPayloadText(), resource);
            if (attributes != null) {
                resource.internal().updateAttributes(attributes);
            }
            LOG.debug("Discovered resource: {}", resource.getPath());
            LOG.debug("Discover message: {}", response.getPayloadText());
        }
    }

    /********************  EXECUTE  ********************/

    public void execute(ObjectResourceProxy<?> resource, String arguments) {
        REQ request = requestBuilder.buildExecuteRequest(resource, arguments);
        PendingRequest pendingRequest = sendRequest(request);

        Lwm2mResponse response = pendingRequest.waitForCompletion();
        if (response.isSuccess()) {
            LOG.debug("Executed resource: {}", resource.getPath());
        }
    }

    /********************  OBSERVE  ********************/

    public void observe(ObjectNodeProxy<?> node, ObservationListener<?> listener) {
        REQ request = requestBuilder.buildObserveRequest(node);
        PendingRequest pendingRequest = sendRequest(request);

        Lwm2mResponse response = pendingRequest.waitForCompletion();
        if (response.isSuccess()) {
            startObserve(request.getToken(), new ObserveHandler(node, listener));
            node.setObserveToken(request.getToken());
            LOG.debug("Started observing: {}", node.getPath());
        }
    }

    public void cancelObserve(ObjectNodeProxy<?> node) {
        REQ request = requestBuilder.buildCancelObserveRequest(node);
        PendingRequest pendingRequest = sendRequest(request);

        Lwm2mResponse response = pendingRequest.waitForCompletion();
        if (response.isSuccess()) {
            stopObserve(node.getObserveToken());
            node.setObserveToken(null);
            LOG.debug("Stopped observing: {}", node.getPath());
        }
    }

    /********************  READ  ********************/

    public void read(ObjectBaseProxy<?> object) {
        REQ request = requestBuilder.buildReadRequest(object);
        PendingRequest pendingRequest = sendRequest(request);

        Lwm2mResponse response = pendingRequest.waitForCompletion();
        if (response.isSuccess()) {
            ObjectBaseProxy newValue = readParser.deserialize(object, response.getPayload());
            object.internal().update(newValue);
            LOG.debug("Read object: {}", object.getPath());
        }
    }

    public void read(ObjectInstanceProxy instance) {
        REQ request = requestBuilder.buildReadRequest(instance);
        PendingRequest pendingRequest = sendRequest(request);

        Lwm2mResponse response = pendingRequest.waitForCompletion();
        if (response.isSuccess()) {
            ObjectInstanceProxy newValue = readParser.deserialize(instance, response.getPayload());
            instance.internal().update(newValue);
            LOG.debug("Read instance: {}", instance.getPath());
        }
    }

    @SuppressWarnings("unchecked")
    public void read(ObjectResourceProxy<?> resource) {
        REQ request = requestBuilder.buildReadRequest(resource);
        PendingRequest pendingRequest = sendRequest(request);

        Lwm2mResponse response = pendingRequest.waitForCompletion();
        if (response.isSuccess()) { // TODO fix client, then operate basing on response.getContentType()
            if (OpaqueResourceValue.class.equals(resource.getValueType())) {
                OpaqueResourceValue newValue = new OpaqueResourceValue(response.getPayload());
                resource.internal().update(newValue);
            } else if (resource instanceof ObjectMultipleResourceProxy) {
                ObjectMultipleResourceProxy newValue = (ObjectMultipleResourceProxy<?>) readParser.deserialize(resource, response.getPayload());
                resource.internal().update(newValue);
            } else {
                ResourceValue<?> newValue = readParser.deserialize(resource.getValue(), response.getPayload());
                resource.internal().update(newValue);
            }
            LOG.debug("Read resource: {}", resource.getPath());
        }
    }

    /********************  WRITE ATTRIBUTES  ********************/

    public void writeAttributes(ObjectNodeProxy<?> node) {
        REQ request = requestBuilder.buildWriteAttributesRequest(node);
        PendingRequest pendingRequest = sendRequest(request);

        Lwm2mResponse response = pendingRequest.waitForCompletion();
        if (response.isSuccess()) {
            LOG.debug("Write attributes: {}", node.getPath());
        }
    }

    /********************  WRITE  ********************/

    public void write(ObjectInstanceProxy instance) {
        REQ request = requestBuilder.buildWriteRequest(instance);
        PendingRequest pendingRequest = sendRequest(request);

        Lwm2mResponse response = pendingRequest.waitForCompletion();
        if (response.isSuccess()) {
            LOG.debug("Write instance: {}", instance.getPath());
        }
    }

    public void write(ObjectResourceProxy resource) {
        REQ request = requestBuilder.buildWriteRequest(resource);
        PendingRequest pendingRequest = sendRequest(request);

        Lwm2mResponse response = pendingRequest.waitForCompletion();
        if (response.isSuccess()) {
            LOG.debug("Write resource: {}", resource.getPath());
        }
    }

    private void connectToRemote(ObjectInstanceProxy instance) {
        ClientProxyImpl client = (ClientProxyImpl) instance.getClientProxy();
        client.getServer().internal().getObjectTreeCreator().connectToRemoteClient(instance, client); // TODO It doesn't look good
    }

    private boolean isCancelObserve(Lwm2mRequest request, Lwm2mResponse response) {
        return (request.getOperation() == LWM2M.Operation.I_CANCEL_OBSERVATION && response.isSuccess());
    }

}
