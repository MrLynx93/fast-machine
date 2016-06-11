package com.agh.fastmachine.server.api.model;

import com.agh.fastmachine.core.internal.model.ObjectBaseModel;
import com.agh.fastmachine.core.internal.visitor.NodeVisitor;
import com.agh.fastmachine.server.api.listener.ObservationListener;

import java.util.HashMap;
import java.util.Map;

public class ObjectBaseProxy<T extends ObjectInstanceProxy> extends ObjectNodeProxy<ObjectBaseProxy.Internal> implements ObjectBaseModel {
    private Map<Integer, ObjectInstanceProxy> instances = new HashMap<>();
    private String path;

    public ObjectBaseProxy(int id) {
        super(id);
        internal = new Internal();
    }

    public ObjectBaseProxy(int id, Map<Integer, ObjectInstanceProxy> instances) {
        super(id);
        this.instances = instances;
        internal = new Internal();
    }

    @Override
    public T getInstance(int id) {
        return (T) instances.get(id);
    }

    @Override
    public Map<Integer, T> getObjectInstances() {
        return (Map<Integer, T>) instances;
    }

    public void read() {
        transportLayer.readOperations(clientProxy).read(this);
    }

    public void discover() {
        transportLayer.discoverOperations(clientProxy).discover(this);
    }

    public void writeAttributes() {
        transportLayer.writeAttributeOperations(clientProxy).writeAttributes(this);
    }

    public void observe(ObservationListener<ObjectBaseProxy<T>> listener) {
        transportLayer.observeOperations(clientProxy).observe(this, listener);
    }

    public void cancelObservation() {
        transportLayer.observeOperations(clientProxy).cancelObservation(this);
    }

    public boolean isChanged() {
        for (ObjectInstanceProxy objectInstance : instances.values()) {
            if (objectInstance.isChanged()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    /// INTERNAL ///

    public class Internal extends ObjectNodeProxy.Internal {

        Internal() {
        }

        public void addInstance(ObjectInstanceProxy instance) {
            ObjectBaseProxy.this.instances.put(instance.getId(), instance);
        }

        public void update(ObjectBaseProxy<?> newObject) {
            for (Map.Entry<Integer, ? extends ObjectInstanceProxy> instanceEntry : newObject.getObjectInstances().entrySet()) {
                Integer instanceId = instanceEntry.getKey();
                ObjectBaseProxy.this.instances.get(instanceId).internal().update(instanceEntry.getValue());
            }
            for (ObjectInstanceProxy instance : ObjectBaseProxy.this.instances.values()) {
                instance.internal().setChanged(false);
            }
        }

        public void setPath(String path) {
            ObjectBaseProxy.this.path = path;
        }

        public String getPath() {
            return path;
        }
    }

    public void addInstance(ObjectInstanceProxy instance) {
        instances.put(instance.getId(), instance);
    }

}
