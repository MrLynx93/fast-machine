package com.agh.fastmachine.server.api.model;

import com.agh.fastmachine.core.internal.model.ObjectResourceModel;
import com.agh.fastmachine.core.api.model.resourcevalue.ResourceValue;
import com.agh.fastmachine.core.internal.visitor.NodeVisitor;
import com.agh.fastmachine.server.api.exception.ObjectDeletedException;
import com.agh.fastmachine.server.api.listener.ObservationListener;

import java.util.Map;

public class ObjectResourceProxy<T extends ResourceValue<?>> extends ObjectNodeProxy<ObjectResourceProxy.Internal> implements ObjectResourceModel<T> {
    private ObjectInstanceProxy instance;
    private T value;
    private Class<T> valueClass;
    protected boolean isChanged = true;
    boolean isDeleted = false;

    public ObjectResourceProxy(int id, T value, Class<T> valueClass) {
        super(id);
        this.value = value;
        this.valueClass = valueClass;
        internal = new Internal();
    }

    @Override
    public ObjectInstanceProxy getInstance() {
        return instance;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public Class<T> getValueType() {
        return valueClass;
    }

    public void setValue(ResourceValue<?> value) {
        if (isDeleted) {
            throw new ObjectDeletedException();
        }
        isChanged = true;
        this.value = (T) value;
    }

    public void read() {
        if (isDeleted) {
            throw new ObjectDeletedException();
        }
        transportLayer.readOperations(clientProxy).read(this);
    }

    public void write() {
        if (isDeleted) {
            throw new ObjectDeletedException();
        }
        if (isChanged) {
            transportLayer.writeOperations(clientProxy).write(this);
            isChanged = false;
        }
    }

    public void discover() {
        if (isDeleted) {
            throw new ObjectDeletedException();
        }
        transportLayer.discoverOperations(clientProxy).discover(this);
    }

    public void writeAttributes() {
        if (isDeleted) {
            throw new ObjectDeletedException();
        }
        transportLayer.writeAttributeOperations(clientProxy).writeAttributes(this);
    }

    public void execute(byte[] arguments) {
        if (isDeleted) {
            throw new ObjectDeletedException();
        }
        transportLayer.executeOperations(clientProxy).execute(this, arguments);
    }

    public void observe(ObservationListener<ObjectResourceProxy<T>> listener) {
        if (isDeleted) {
            throw new ObjectDeletedException();
        }
        transportLayer.observeOperations(clientProxy).observe(this, listener);
    }

    public void cancelObservation() {
        transportLayer.observeOperations(clientProxy).cancelObservation(this);
    }

    public boolean isChanged() {
        return isChanged;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    /// INTERNAL ///

    public class Internal extends ObjectNodeProxy.Internal {
        Internal() {
        }

        public ObjectInstanceProxy getInternalInstance() {
            return instance;
        }

        void setChanged(boolean isChanged) {
            ObjectResourceProxy.this.isChanged = isChanged;
        }

        public void setInstance(ObjectInstanceProxy instance) {
            ObjectResourceProxy.this.instance = instance;
        }

        public void update(ResourceValue<?> newValue) {
            ObjectResourceProxy.this.value = (T) newValue;
            ObjectResourceProxy.this.isChanged = false;
        }

        public void update(ObjectMultipleResourceProxy<?> newValue) {
            if (ObjectResourceProxy.this instanceof ObjectMultipleResourceProxy) {
                ((ObjectMultipleResourceProxy)ObjectResourceProxy.this).values.clear();
                ((ObjectMultipleResourceProxy)ObjectResourceProxy.this).values.putAll((Map) newValue.values);
                ObjectResourceProxy.this.isChanged = false;
            }
        }
    }

}
