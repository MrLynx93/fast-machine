package com.agh.fastmachine.server.api.model;

import com.agh.fastmachine.core.internal.model.ObjectResourceModel;
import com.agh.fastmachine.core.api.model.resourcevalue.ResourceValue;
import com.agh.fastmachine.core.internal.visitor.NodeVisitor;
import com.agh.fastmachine.server.api.exception.ObjectDeletedException;
import com.agh.fastmachine.server.api.listener.ObservationListener;
import com.agh.fastmachine.server.internal.transport.LWM2M;
import com.agh.fastmachine.server.internal.transport.stats.TimeoutException;

import java.util.Map;

public class ObjectResourceProxy<T extends ResourceValue<?>> extends ObjectNodeProxy<ObjectResourceProxy.Internal> implements ObjectResourceModel<T> {
    private ObjectInstanceProxy instance;
    private T value;
    private Class<T> valueClass;
    public boolean isChanged = true;
    boolean isDeleted = false;
    private String name;

    public ObjectResourceProxy(int id, T value, Class<T> valueClass) {
        super(id);
        this.value = value;
        this.valueClass = valueClass;
        internal = new Internal();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isMultiple() {
        return false;
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

    @Override
    public LWM2M.Path getPath() {
        return LWM2M.Path.of(getInstance().getObject().getId(), getInstance().getId(), id);
    }

    public void setValue(ResourceValue<?> value) {
        if (isDeleted) {
            throw new ObjectDeletedException();
        }
        isChanged = true;
        this.value = (T) value;
    }

    public void read() throws TimeoutException {
        if (isDeleted) {
            throw new ObjectDeletedException();
        }
        transport.read(clientProxy, this);
    }

    public void readAll() {
        if (isDeleted) {
            throw new ObjectDeletedException();
        }
        transport.readAll(clientProxy.getServer(), this);
    }

    public void write() {
        if (isDeleted) {
            throw new ObjectDeletedException();
        }
        if (isChanged) {
            transport.write(clientProxy, this);
            isChanged = false;
        }
    }

    public void writeAll() {
        if (isDeleted) {
            throw new ObjectDeletedException();
        }
        if (isChanged) {
            transport.writeAll(clientProxy.getServer(), this);
            isChanged = false;
        }
    }

    public void discover() {
        if (isDeleted) {
            throw new ObjectDeletedException();
        }
        transport.discover(clientProxy, this);
    }

    public void writeAttributes() {
        if (isDeleted) {
            throw new ObjectDeletedException();
        }
        transport.writeAttributes(clientProxy, this);
    }

    public void execute(String arguments) {
        if (isDeleted) {
            throw new ObjectDeletedException();
        }
        transport.execute(clientProxy, this, arguments);
    }

    public void observe(ObservationListener<ObjectResourceProxy<T>> listener) {
        if (isDeleted) {
            throw new ObjectDeletedException();
        }
        transport.observe(clientProxy, this, listener);
    }

    public void cancelObservation() {
        transport.cancelObserve(clientProxy, this);
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
