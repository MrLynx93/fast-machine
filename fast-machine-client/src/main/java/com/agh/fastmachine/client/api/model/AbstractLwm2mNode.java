package com.agh.fastmachine.client.api.model;

import com.agh.fastmachine.client.api.Client;
import com.agh.fastmachine.client.internal.access.ReadAccessFilter;
import com.agh.fastmachine.client.internal.access.ServerAccessVerifier;
import com.agh.fastmachine.client.internal.access.WriteAccessVerifier;
import com.agh.fastmachine.client.internal.exception.*;
import com.agh.fastmachine.client.internal.message.request.Lwm2mContentRequest;
import com.agh.fastmachine.client.internal.message.request.Lwm2mRequest;
import com.agh.fastmachine.client.internal.message.request.Lwm2mWriteAttributeRequest;
import com.agh.fastmachine.client.internal.message.response.Lwm2mCreateResponse;
import com.agh.fastmachine.client.internal.message.response.Lwm2mObserveResponse;
import com.agh.fastmachine.client.internal.message.response.Lwm2mResponse;
import com.agh.fastmachine.client.api.model.builtin.ServerObjectInstance;
import com.agh.fastmachine.client.internal.parser.DiscoverSerializer;
import com.agh.fastmachine.client.internal.visitor.merge.Lwm2mNodeMerger;
import com.agh.fastmachine.client.internal.service.observation.ObserveSession;
import com.agh.fastmachine.client.internal.visitor.*;
import com.agh.fastmachine.core.api.model.Attributes;
import com.agh.fastmachine.client.internal.attribute.AttributesFactory;
import com.agh.fastmachine.core.internal.model.ObjectNodeModel;
import com.agh.fastmachine.client.internal.parser.ClientReadParser;
import com.agh.fastmachine.core.internal.parser.ReadParser;
import com.agh.fastmachine.core.internal.parser.WriteParser;
import com.agh.fastmachine.core.api.model.Operations;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractLwm2mNode implements VisitableObjectNode, ObjectNodeModel {
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    protected int id;
    protected Client client;
    protected Lwm2mNodeCoapResource coapResource;

    protected Map<Integer, Attributes> writeAttributes = new HashMap<>();
    protected Map<Integer, ObserveSession> observeSessions = new ConcurrentHashMap<>();

    private final ReadParser readParser = new ClientReadParser();

    @Override
    public int getId() {
        return id;
    }

    public abstract void notifyObservers(Integer shortServerId);

    public Lwm2mResponse handleRead(Lwm2mRequest request) {
        try {
            int shortServerId = getShortServerId(request);

            AbstractLwm2mNode node;
            node = ServerAccessVerifier.checkAccessRights(this, shortServerId, Operations.READ);
            node = ReadAccessFilter.filter(node);
            byte[] bytes = serializeNode(node);
            LOG.debug("Server {} executed read on node {}", request.getServerUri(), coapResource.getPath());
            return new Lwm2mResponse(CoAP.ResponseCode.CONTENT, bytes);

        } catch (ServerAccessRightsException e) {
            LOG.error("Server {} is not authorized to read on node {}", request.getServerUri(), coapResource.getPath());
            return new Lwm2mResponse(CoAP.ResponseCode.UNAUTHORIZED);

        } catch (ResourcesAccessRightsException e) {
            LOG.error("Read is not allowed on node {}", coapResource.getPath());
            return new Lwm2mResponse(CoAP.ResponseCode.METHOD_NOT_ALLOWED);

        } catch (SerializeLwm2mNodeException e) {
            LOG.error("Node {} not found while read from server {}", coapResource.getPath(), request.getServerUri());
            return new Lwm2mResponse(CoAP.ResponseCode.NOT_FOUND);

        } catch (ServerIdNotFoundException e) {
            LOG.error("Server {} is not set in LWM2M read request", request.getServerUri(), coapResource.getPath());
            return new Lwm2mResponse(CoAP.ResponseCode.UNAUTHORIZED);
        }
    }

    public Lwm2mResponse handleWrite(Lwm2mContentRequest request) {
        try {
            int shortServerId = getShortServerId(request);

            AbstractLwm2mNode node, parsedNode;
            node = ServerAccessVerifier.checkAccessRights(this, shortServerId, Operations.WRITE);
            parsedNode = readParser.deserialize(this, request.getByteContent());
            WriteAccessVerifier.checkWriteAccess(node, parsedNode);
            Lwm2mNodeMerger.merge(this, parsedNode);
            LOG.debug("Server {} executed write on node {}", request.getServerUri(), coapResource.getPath());
            return new Lwm2mResponse(CoAP.ResponseCode.CHANGED);

        } catch (ServerIdNotFoundException e) {
            LOG.error("Server {} is not set in LWM2M write request", request.getServerUri(), coapResource.getPath());
            return new Lwm2mResponse(CoAP.ResponseCode.UNAUTHORIZED);

        } catch (ServerAccessRightsException e) {
            LOG.error("Server {} is not authorized to write to node {}", request.getServerUri(), coapResource.getPath());
            return new Lwm2mResponse(CoAP.ResponseCode.UNAUTHORIZED);

        } catch (ResourcesAccessRightsException e) {
            LOG.error("Write is not allowed on node {}", coapResource.getPath());
            return new Lwm2mResponse(CoAP.ResponseCode.METHOD_NOT_ALLOWED);
        }
    }

    public Lwm2mResponse handleWriteAttributes(Lwm2mWriteAttributeRequest request) {
        try {
            int shortServerId = getShortServerId(request);
            if (request.hasCancel()) {
                ObserveSession observeSession = observeSessions.get(shortServerId);
                client.getObservationInterface().stopObserving(observeSession);
                observeSessions.remove(shortServerId);
                LOG.debug("Observing node {} was cancelled by server {}", coapResource.getPath(), request.getServerUri());
                return new Lwm2mResponse(CoAP.ResponseCode.DELETED); // TODO response code???
            } else {
                Attributes newAttributes = AttributesFactory.merge(this, request);
                writeAttributes.put(shortServerId, newAttributes);
                return new Lwm2mResponse(CoAP.ResponseCode.CHANGED);
            }
        } catch (ServerIdNotFoundException e) {
            LOG.error("Server is not registered on client");
            return new Lwm2mResponse(CoAP.ResponseCode.UNAUTHORIZED);
        } catch (UnsupportedWriteAttributeException e) {
            LOG.error("Cannot override attribute {}", e.getMessage());
            return new Lwm2mResponse(CoAP.ResponseCode.METHOD_NOT_ALLOWED);
        }
    }

    public Lwm2mObserveResponse handleObserve(Lwm2mRequest request) {
        ObserveSession observeSession = client.getObservationInterface().startObserving(this, request);
        observeSessions.put(request.getRequestingServer().shortServerId.getValue().value, observeSession);
        LOG.error("Server {} started observing node {}", request.getServerUri(), coapResource.getPath());
        return new Lwm2mObserveResponse();
    }

    public Lwm2mResponse handleDiscover(Lwm2mRequest request) {
        String discoverResponse = DiscoverSerializer.createDiscoverResponse(this);
        LOG.debug("Server {} executed discover on node {}", request.getServerUri(), coapResource.getPath());
        return new Lwm2mResponse(CoAP.ResponseCode.CONTENT, discoverResponse.getBytes(Charset.forName("UTF-8")), MediaTypeRegistry.APPLICATION_LINK_FORMAT);
    }

    public Lwm2mCreateResponse handleCreate(Lwm2mContentRequest request, Integer requestedId) {
        LOG.error("Create on node {} returning \'Method Not Allowed\'", coapResource.getPath());
        return new Lwm2mCreateResponse(CoAP.ResponseCode.METHOD_NOT_ALLOWED, "");
    }

    public Lwm2mResponse handleExecute(Lwm2mContentRequest request) {
        LOG.error("Client did not override handleExecute method. Returning \'Method Not Allowed\' to server");
        return new Lwm2mResponse(CoAP.ResponseCode.METHOD_NOT_ALLOWED);
    }

    public Lwm2mResponse handleDelete(Lwm2mRequest request) {
        LOG.error("Delete on node {} returning \'Method Not Allowed\'", coapResource.getPath());
        return new Lwm2mResponse(CoAP.ResponseCode.METHOD_NOT_ALLOWED);
    }

    /// GETTERS AND SETTERS ///

    public int getNumberOfServers() {
        return writeAttributes.size();
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Client getClient() {
        return client;
    }

    void setCoapResource(Lwm2mNodeCoapResource coapResource) {
        this.coapResource = coapResource;
    }

    Lwm2mNodeCoapResource getCoapResource() {
        return coapResource;
    }

    public Attributes getWriteAttributes(int shortServerId) {
        return writeAttributes.get(shortServerId);
    }

    public Map<Integer, Attributes> getWriteAttributes() {
        return Collections.unmodifiableMap(writeAttributes);
    }

    boolean isObserved(int shortServerID) {
        return observeSessions.containsKey(shortServerID);
    }

    /// PRIVATE ///

    protected int getShortServerId(Lwm2mRequest request) throws ServerIdNotFoundException {
        ServerObjectInstance requestingServer = request.getRequestingServer();
        if (requestingServer == null) {
            throw new ServerIdNotFoundException();
        }
        return requestingServer.shortServerId.getValue().value;
    }

    private byte[] serializeNode(AbstractLwm2mNode node) throws SerializeLwm2mNodeException {
        byte[] bytes = WriteParser.serialize(node);
        if (bytes == null) {
            throw new SerializeLwm2mNodeException();
        }
        return bytes;
    }

}




