package com.agh.fastmachine.server.internal.transport;

import com.agh.fastmachine.core.api.model.Attributes;
import com.agh.fastmachine.core.api.model.resourcevalue.OpaqueResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.ResourceValue;
import com.agh.fastmachine.core.internal.parser.ReadParser;
import com.agh.fastmachine.core.internal.parser.WriteAttributesParser;
import com.agh.fastmachine.server.api.ClientProxy;
import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.api.listener.ObservationListener;
import com.agh.fastmachine.server.api.model.*;
import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.internal.parser.ServerReadParser;
import com.agh.fastmachine.server.internal.transport.stats.Event;
import com.agh.fastmachine.server.internal.transport.stats.Stats;
import com.agh.fastmachine.server.internal.transport.stats.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Transport<T extends TransportConfiguration, REQ extends Lwm2mRequest> {
    private static final Logger LOG = LoggerFactory.getLogger(Transport.class);
    protected Map<String, ClientProxyImpl> registeredClients = new HashMap<>();

    protected final Map<String, PendingRequest> pendingRequests = new ConcurrentHashMap<>();
    private final Map<String, ObserveHandler> observeHandlers = new ConcurrentHashMap<>();
    private final WriteAttributesParser writeAttributesParser = new WriteAttributesParser();
    protected final ReadParser readParser = new ServerReadParser();
    protected RequestBuilder<REQ> requestBuilder;
    public Stats stats = new Stats();
    protected T configuration;
    protected Server server;

    public abstract void start(T configuration);

    public abstract void stop();

    protected abstract void doSendRequest(ClientProxyImpl client, REQ request) throws Exception;

    public PendingRequest sendRequest(ClientProxyImpl client, REQ request) {
        PendingRequest pendingRequest = new PendingRequest(request, client);
        pendingRequests.put(request.getToken(), pendingRequest);
        try {
            doSendRequest(client, request);
        } catch (Exception e) {
            stats.addEvent(client, Event.downlinkRequestSendTimeout(request.getOperation()));
            e.printStackTrace();
            LOG.error("Failed to send request {}", request);
        }
        return pendingRequest;
    }

    protected void handleResponse(Lwm2mResponse response) {
        String token = response.getToken();
        PendingRequest pendingRequest = pendingRequests.get(token);
        Lwm2mRequest request = pendingRequest.getRequest();

        if (pendingRequest.isBroadcast()) {
            stats.addBroadcastEvent(server, Event.downlinkResponseReceiveSuccess(request.getOperation()));
        } else {
            stats.addEvent(pendingRequest.getClient(), Event.downlinkResponseReceiveSuccess(request.getOperation()));
        }

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

    public abstract void createAll(Server server, Server.InstanceCreator instance);

    public abstract void createAll(Server server, Server.InstanceCreator instance, int instanceId);

    public void create(ClientProxyImpl client, ObjectInstanceProxy instance) {
        REQ request = requestBuilder.buildCreateRequest(instance);
        doCreate(client, instance, request);
    }

    public void create(ClientProxyImpl client, ObjectInstanceProxy instance, int instanceId) {
        REQ request = requestBuilder.buildCreateRequest(instance, instanceId);
        doCreate(client, instance, request);
    }

    private void doCreate(ClientProxyImpl client, ObjectInstanceProxy instance, REQ request) {
        PendingRequest pendingRequest = sendRequest(client, request);
        try {
            Lwm2mResponse response = pendingRequest.waitForCompletion();
            if (response.isSuccess()) {
                instance.internal().setId(response.getCreatedInstanceId());
                connectToRemote(instance);
                LOG.debug("Created object instance: {}", instance.getPath());
            }
        } catch (TimeoutException e) {
            stats.addEvent(client, Event.downlinkResponseReceiveTimeout(request.getOperation()));
            LOG.error("Didn't receive response for {}", request);
        }
    }

    /********************  DELETE  ********************/

    public void delete(ClientProxyImpl client, ObjectInstanceProxy instance) {
        REQ request = requestBuilder.buildDeleteRequest(instance);
        PendingRequest pendingRequest = sendRequest(client, request);

        try {
            Lwm2mResponse response = pendingRequest.waitForCompletion();
            if (response.isSuccess()) {
                LOG.debug("Deleted instance: {}", instance.getPath());
            }
        } catch (TimeoutException e) {
            stats.addEvent(client, Event.downlinkResponseReceiveTimeout(request.getOperation()));
            LOG.error("Didn't receive response for {}", request);
        }
    }

    /********************  DISCOVER  ********************/

    public void discover(ClientProxyImpl client, ObjectBaseProxy<?> object) {
        REQ request = requestBuilder.buildDiscoverRequest(object);
        PendingRequest pendingRequest = sendRequest(client, request);

        try {
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
        } catch (TimeoutException e) {
            stats.addEvent(client, Event.downlinkResponseReceiveTimeout(request.getOperation()));
            LOG.error("Didn't receive response for {}", request);
        }
    }

    public void discover(ClientProxyImpl client, ObjectInstanceProxy instance) {
        REQ request = requestBuilder.buildDiscoverRequest(instance);
        PendingRequest pendingRequest = sendRequest(client, request);

        try {
            Lwm2mResponse response = pendingRequest.waitForCompletion();
            if (response.isSuccess()) {
                Attributes attributes = writeAttributesParser.parseWriteAttributes(response.getPayloadText());
                if (attributes != null) {
                    instance.internal().updateAttributes(attributes);
                }
                LOG.debug("Discovered instance: {}", instance.getPath());
                LOG.debug("Discover message: {}", response.getPayloadText());
            }
        } catch (TimeoutException e) {
            stats.addEvent(client, Event.downlinkResponseReceiveTimeout(request.getOperation()));
            LOG.error("Didn't receive response for {}", request);
        }
    }

    public void discover(ClientProxyImpl client, ObjectResourceProxy<?> resource) {
        REQ request = requestBuilder.buildDiscoverRequest(resource);
        PendingRequest pendingRequest = sendRequest(client, request);

        try {
            Lwm2mResponse response = pendingRequest.waitForCompletion();
            if (response.isSuccess()) {
                Attributes attributes = writeAttributesParser.parseResourceWriteAttributes(response.getPayloadText(), resource);
                if (attributes != null) {
                    resource.internal().updateAttributes(attributes);
                }
                LOG.debug("Discovered resource: {}", resource.getPath());
                LOG.debug("Discover message: {}", response.getPayloadText());
            }
        } catch (TimeoutException e) {
            stats.addEvent(client, Event.downlinkResponseReceiveTimeout(request.getOperation()));
            LOG.error("Didn't receive response for {}", request);
        }
    }

    /********************  EXECUTE  ********************/

    public void execute(ClientProxyImpl client, ObjectResourceProxy<?> resource, String arguments) {
        REQ request = requestBuilder.buildExecuteRequest(resource, arguments);
        PendingRequest pendingRequest = sendRequest(client, request);

        try {
            Lwm2mResponse response = pendingRequest.waitForCompletion();
            if (response.isSuccess()) {
                LOG.debug("Executed resource: {}", resource.getPath());
            }
        } catch (TimeoutException e) {
            stats.addEvent(client, Event.downlinkResponseReceiveTimeout(request.getOperation()));
            LOG.error("Didn't receive response for {}", request);
        }
    }

    /********************  OBSERVE  ********************/

    public void observe(ClientProxyImpl client, ObjectNodeProxy<?> node, ObservationListener<?> listener) {
        REQ request = requestBuilder.buildObserveRequest(node);
        PendingRequest pendingRequest = sendRequest(client, request);

        try {
            Lwm2mResponse response = pendingRequest.waitForCompletion();
            if (response.isSuccess()) {
                startObserve(request.getToken(), new ObserveHandler(node, listener));
                node.setObserveToken(request.getToken());
                LOG.debug("Started observing: {}", node.getPath());
            }
        } catch (TimeoutException e) {
            stats.addEvent(client, Event.downlinkResponseReceiveTimeout(request.getOperation()));
            LOG.error("Didn't receive response for {}", request);
        }
    }

    public void cancelObserve(ClientProxyImpl client, ObjectNodeProxy<?> node) {
        REQ request = requestBuilder.buildCancelObserveRequest(node);
        PendingRequest pendingRequest = sendRequest(client, request);

        try {
            Lwm2mResponse response = pendingRequest.waitForCompletion();
            if (response.isSuccess()) {
                stopObserve(node.getObserveToken());
                node.setObserveToken(null);
                LOG.debug("Stopped observing: {}", node.getPath());
            }
        } catch (TimeoutException e) {
            stats.addEvent(client, Event.downlinkResponseReceiveTimeout(request.getOperation()));
            LOG.error("Didn't receive response for {}", request);
        }
    }

    /********************  READ  ********************/

    public abstract void readAll(Server server, ObjectBaseProxy<?> object);

    public abstract void readAll(Server server, ObjectInstanceProxy instance);

    public abstract void readAll(Server server, ObjectResourceProxy<?> resource);

    public void read(ClientProxyImpl client, ObjectBaseProxy object) {
        REQ request = requestBuilder.buildReadRequest(object);
        PendingRequest pendingRequest = sendRequest(client, request);

        try {
            Lwm2mResponse response = pendingRequest.waitForCompletion();
            if (response.isSuccess()) {
                ObjectBaseProxy newValue = readParser.deserialize(object, response.getPayload());
                ((ObjectBaseProxy<?>)object).internal().update(newValue);
                LOG.debug("Read object: {}", object.getPath());
            }
        } catch (TimeoutException e) {
            stats.addEvent(client, Event.downlinkResponseReceiveTimeout(request.getOperation()));
            LOG.error("Didn't receive response for {}", request);
        }
    }

    public void read(ClientProxyImpl client, ObjectInstanceProxy instance) {
        REQ request = requestBuilder.buildReadRequest(instance);
        PendingRequest pendingRequest = sendRequest(client, request);

        try {
            Lwm2mResponse response = pendingRequest.waitForCompletion();
            if (response.isSuccess()) {
                ObjectInstanceProxy newValue = readParser.deserialize(instance, response.getPayload());
                instance.internal().update(newValue);
                LOG.debug("Read instance: {}", instance.getPath());
            }
        } catch (TimeoutException e) {
            stats.addEvent(client, Event.downlinkResponseReceiveTimeout(request.getOperation()));
            LOG.error("Didn't receive response for {}", request);
        }
    }

    @SuppressWarnings("unchecked")
    public void read(ClientProxyImpl client, ObjectResourceProxy<?> resource) {
        REQ request = requestBuilder.buildReadRequest(resource);
        PendingRequest pendingRequest = sendRequest(client, request);

        try {
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
        } catch (TimeoutException e) {
            stats.addEvent(client, Event.downlinkResponseReceiveTimeout(request.getOperation()));
            LOG.error("Didn't receive response for {}", request);
        }
    }

    /********************  WRITE ATTRIBUTES  ********************/

    public void writeAttributes(ClientProxyImpl client, ObjectNodeProxy<?> node) {
        REQ request = requestBuilder.buildWriteAttributesRequest(node);
        PendingRequest pendingRequest = sendRequest(client, request);

        try {
            Lwm2mResponse response = pendingRequest.waitForCompletion();
            if (response.isSuccess()) {
                LOG.debug("Write attributes: {}", node.getPath());
            }
        } catch (TimeoutException e) {
            stats.addEvent(client, Event.downlinkResponseReceiveTimeout(request.getOperation()));
            LOG.error("Didn't receive response for {}", request);
        }
    }

    /********************  WRITE  ********************/

    public abstract void writeAll(Server server, ObjectInstanceProxy instance);

    public abstract void writeAll(Server server, ObjectResourceProxy resource);

    public void write(ClientProxyImpl client, ObjectInstanceProxy instance) {
        REQ request = requestBuilder.buildWriteRequest(instance);
        PendingRequest pendingRequest = sendRequest(client, request);

        try {
            Lwm2mResponse response = pendingRequest.waitForCompletion();
            if (response.isSuccess()) {
                LOG.debug("Write instance: {}", instance.getPath());
            }
        } catch (TimeoutException e) {
            stats.addEvent(client, Event.downlinkResponseReceiveTimeout(request.getOperation()));
            LOG.error("Didn't receive response for {}", request);
        }
    }

    public void write(ClientProxyImpl client, ObjectResourceProxy resource) {
        REQ request = requestBuilder.buildWriteRequest(resource);
        PendingRequest pendingRequest = sendRequest(client, request);

        try {
            Lwm2mResponse response = pendingRequest.waitForCompletion();
            if (response.isSuccess()) {
                LOG.debug("Write resource: {}", resource.getPath());
            }
        } catch (TimeoutException e) {
            stats.addEvent(client, Event.downlinkResponseReceiveTimeout(request.getOperation()));
            LOG.error("Didn't receive response for {}", request);
        }
    }

    protected void connectToRemote(ObjectInstanceProxy instance) {
        ClientProxyImpl client = (ClientProxyImpl) instance.getClientProxy();
        client.getServer().internal().getObjectTreeCreator().connectToRemoteClient(instance, client);
    }

    private boolean isCancelObserve(Lwm2mRequest request, Lwm2mResponse response) {
        return (request.getOperation() == LWM2M.Operation.I_CANCEL_OBSERVATION && response.isSuccess());
    }

}
