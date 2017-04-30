package com.agh.fastmachine.server.api.model;

import com.agh.fastmachine.core.internal.model.ObjectBaseModel;
import com.agh.fastmachine.core.internal.visitor.NodeVisitor;
import com.agh.fastmachine.server.api.listener.ObservationListener;
import com.agh.fastmachine.server.internal.transport.LWM2M;

import java.util.HashMap;
import java.util.Map;

public class ObjectBaseProxy<T extends ObjectInstanceProxy> extends ObjectNodeProxy<ObjectBaseProxy.Internal> implements ObjectBaseModel {
    private Map<Integer, ObjectInstanceProxy> instances = new HashMap<>();
    private String path;
    private String name;

    public ObjectBaseProxy(int id) {
        super(id);
        internal = new Internal();
    }

    public ObjectBaseProxy(int id, Map<Integer, ObjectInstanceProxy> instances) {
        super(id);
        this.instances = instances;
        internal = new Internal();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        transport.read(clientProxy, this);
    }

    public void readAll() {
        transport.readAll(clientProxy.getServer(), this);
    }

    public void discover() {
        transport.discover(clientProxy, this);
    }

    public void writeAttributes() {
        transport.writeAttributes(clientProxy, this);
    }

    @Override
    public LWM2M.Path getPath() {
        return LWM2M.Path.of(id);
    }

    public void observe(ObservationListener<ObjectBaseProxy<T>> listener) {
        transport.observe(clientProxy, this, listener);
    }

    public void cancelObservation() {
        transport.cancelObserve(clientProxy, this);
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

    }

    public void addInstance(ObjectInstanceProxy instance) {
        instances.put(instance.getId(), instance);
    }

}
