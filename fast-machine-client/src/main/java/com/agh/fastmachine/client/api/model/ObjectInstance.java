package com.agh.fastmachine.client.api.model;

import com.agh.fastmachine.client.api.model.builtin.AccessControlObjectInstance;
import com.agh.fastmachine.client.internal.service.observation.ObserveSession;
import com.agh.fastmachine.client.internal.visitor.ObjectNodeVisitor;
import com.agh.fastmachine.core.api.model.annotation.Lwm2mResource;
import com.agh.fastmachine.core.api.model.annotation.Mandatory;
import com.agh.fastmachine.core.internal.model.ObjectInstanceModel;
import com.agh.fastmachine.core.internal.model.ObjectResourceModel;
import com.agh.fastmachine.core.internal.visitor.NodeVisitor;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class ObjectInstance extends AbstractLwm2mNode implements ObjectInstanceModel {
    private Map<Integer, ObjectResource<?>> resources;
    private ObjectBase<?> parent;
    private AccessControlObjectInstance accessControlObjectInstance;

    public ObjectInstance(int id) {
        this.id = id;
    }

    public ObjectInstance(int id, Map<Integer, ObjectResourceModel<?>> resources) {
        this.id = id;
    }

    @Override
    public void notifyObservers(Integer shortServerId) {
        ObserveSession observeSession = observeSessions.get(shortServerId);
        if(observeSession != null) {
            client.getObservationInterface().executeNotify(observeSession);
        }
        parent.notifyObservers(shortServerId);
    }

    public ObjectBase<?> parent() {
        return parent;
    }

    void gatherResources() {
        resources = new HashMap<>();
        try {
            for (Field field : getClass().getFields()) {
                if (field.getAnnotation(Lwm2mResource.class) != null) {
                    ObjectResource resource = (ObjectResource) field.get(this);
                    initResource(resource, field);
                    resource.setParent(this);
                    addResource(resource);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void initResource(ObjectResource resource, Field field) {
        Lwm2mResource annotation = field.getAnnotation(Lwm2mResource.class);
        resource.setId(annotation.id());
        resource.setClazz(getResourceValueClass(field));
        resource.setAllowedOperations(annotation.permissions());
        resource.setIsMandatory(field.getAnnotation(Mandatory.class) != null);
    }

    private Class<?> getResourceValueClass(Field field) {
        ParameterizedType parametrizedType = (ParameterizedType) field.getGenericType();
        return (Class<?>) parametrizedType.getActualTypeArguments()[0];
    }

    public void addResource(ObjectResource resource) {
        resources.put(resource.getId(), resource);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void accept(ObjectNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public Map<Integer, ObjectResourceModel<?>> getResources() {
        return Collections.unmodifiableMap((Map) resources);
    }

    public Map<Integer, ObjectResource<?>> getObjectResources() {
        return Collections.unmodifiableMap(resources);
    }

    public ObjectResource<?> getResource(int id) {
        return resources.get(id);
    }

    public AccessControlObjectInstance getAccessControlObjectInstance() {
        return accessControlObjectInstance;
    }

    void setAccessControlObjectInstance(AccessControlObjectInstance accessControlObjectInstance) {
        this.accessControlObjectInstance = accessControlObjectInstance;
    }

    public void setParent(ObjectBase parent) {
        this.parent = parent;
    }
}