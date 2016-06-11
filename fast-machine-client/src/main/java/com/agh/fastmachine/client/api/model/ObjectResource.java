package com.agh.fastmachine.client.api.model;


import com.agh.fastmachine.client.internal.access.ServerAccessVerifier;
import com.agh.fastmachine.core.api.model.Attributes;
import com.agh.fastmachine.client.internal.attribute.AttributesFactory;
import com.agh.fastmachine.client.internal.message.request.Lwm2mWriteAttributeRequest;
import com.agh.fastmachine.client.internal.message.response.Lwm2mResponse;
import com.agh.fastmachine.client.internal.exception.ServerAccessRightsException;
import com.agh.fastmachine.client.internal.exception.ServerIdNotFoundException;
import com.agh.fastmachine.client.api.model.builtin.ServerObjectInstance;
import com.agh.fastmachine.client.internal.service.observation.NumericObservationResolver;
import com.agh.fastmachine.client.internal.visitor.ObjectNodeVisitor;
import com.agh.fastmachine.core.internal.model.ObjectResourceModel;
import com.agh.fastmachine.core.api.model.resourcevalue.ResourceValue;
import com.agh.fastmachine.core.api.model.Operations;
import com.agh.fastmachine.core.internal.visitor.NodeVisitor;
import org.eclipse.californium.core.coap.CoAP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ObjectResource<T extends ResourceValue<?>> extends AbstractLwm2mNode implements ObjectResourceModel<T> {
    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private T value;
    private boolean isMandatory;
    private Class<T> clazz;
    private ObjectInstance parent;
    protected int allowedOperations;
    protected NumericObservationResolver numericObservationResolver;

    public ObjectResource() {
    }

    /// INTERNAL

    void setId(int id) {
        this.id = id;
    }

    void setIsMandatory(boolean isMandatory) {
        this.isMandatory = isMandatory;
    }

    void setClazz(Class<T> clazz) {
        this.clazz = clazz;
    }

    void setAllowedOperations(int allowedOperations) {
        this.allowedOperations = allowedOperations;
    }


    public ObjectResource(int id, Class<T> clazz) {
        this.id = id;
        this.clazz = clazz;
    }

    public ObjectResource(int id, boolean isMandatory, Class<T> clazz) {
        this.id = id;
        this.isMandatory = isMandatory;
        this.clazz = clazz;
    }

    public ObjectResource(int id, boolean isMandatory, int allowedOperations, Class<T> clazz) {
        this.id = id;
        this.isMandatory = isMandatory;
        this.allowedOperations = allowedOperations;
        this.clazz = clazz;
    }

    public ObjectResource(int id, T value, Class<T> valueClass) {
        this(id, valueClass);
        this.value = value;
    }

    @Override
    public T getValue() {
        return this.value;
    }

    public T getValue(int id) {
        return this.value;
    }

    public int getAllowedOperations() {
        return allowedOperations;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    @Override
    public Class<T> getValueType() {
        return clazz;
    }

    @Override
    public ObjectInstance getInstance() {
        return parent;
    }

    public ObjectInstance parent() {
        return parent;
    }

    public void setValue(ResourceValue<?> newValue) {
        this.value = (T) newValue;
        if (client != null) {
            for (ServerObjectInstance server : client.getServerObjectResolver().getRegisteredServers()) {
                if (numericObservationResolver.shouldNotify(server.getShortServerId())) {
                    notifyObservers(server.getShortServerId());
                }
            }
        }
    }

    public Lwm2mResponse handleWriteAttributes(Lwm2mWriteAttributeRequest request) {
        try {
            int shortServerId = getShortServerId(request);
            ServerAccessVerifier.checkAccessRights(this, shortServerId, Operations.READ);
            if (request.hasCancel()) {
                client.getObservationInterface().stopObserving(observeSessions.get(shortServerId));
                observeSessions.remove(shortServerId);
                LOG.debug("Server {} cancelled observing node {} by writing attributes with \'cancel\'", shortServerId, coapResource.getPath());
            }
            Attributes newAttributes = AttributesFactory.merge(this, request);
            writeAttributes.put(request.getRequestingServer().shortServerId.getValue().value, newAttributes);
            LOG.debug("Server {} wrote attributes to node {}", request.getServerUri(), coapResource.getPath());
            return new Lwm2mResponse(CoAP.ResponseCode.CHANGED);

        } catch (ServerAccessRightsException e) {
            LOG.error("Server {} is not authorized to write attributes to node {}", request.getServerUri(), coapResource.getPath());
            return new Lwm2mResponse(CoAP.ResponseCode.UNAUTHORIZED);

        } catch (ServerIdNotFoundException e) {
            LOG.error("Server {} is not set in LWM2M read request", request.getServerUri(), coapResource.getPath());
            return new Lwm2mResponse(CoAP.ResponseCode.UNAUTHORIZED);
        }
    }

    @Override
    public void notifyObservers(Integer shortServerId) {
        if (isObserved(shortServerId)) {
            client.getObservationInterface().executeNotify(observeSessions.get(shortServerId));
        }
        parent.notifyObservers(shortServerId);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void accept(ObjectNodeVisitor visitor) {
        visitor.visit(this);
    }

    public void initObservationResolver() {
        this.numericObservationResolver = new NumericObservationResolver<>(this);
    }

    public void setParent(ObjectInstance parent) {
        this.parent = parent;
    }
}
