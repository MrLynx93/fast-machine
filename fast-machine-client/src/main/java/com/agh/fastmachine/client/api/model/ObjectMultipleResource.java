package com.agh.fastmachine.client.api.model;

import com.agh.fastmachine.client.api.model.builtin.ServerObjectInstance;
import com.agh.fastmachine.client.internal.visitor.ObjectNodeVisitor;
import com.agh.fastmachine.core.internal.model.ObjectMultipleResourceModel;
import com.agh.fastmachine.core.api.model.resourcevalue.ResourceValue;
import com.agh.fastmachine.core.internal.visitor.NodeVisitor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectMultipleResource<T extends ResourceValue<?>> extends ObjectResource<T> implements ObjectMultipleResourceModel<T> {
    private Map<Integer, T> values = new HashMap<>();

    public ObjectMultipleResource() {
    }

    public ObjectMultipleResource(int id, Class<T> clazz) {
        super(id, clazz);
    }

    public ObjectMultipleResource(int id, boolean isMandatory, Class<T> clazz) {
        super(id, isMandatory, clazz);
    }

    public ObjectMultipleResource(int id, boolean isMandatory, int allowedOperations, Class<T> clazz) {
        super(id, isMandatory, allowedOperations, clazz);
    }

    public ObjectMultipleResource(int id, Map<Integer, T> values, Class<T> valueClass) {
        this(id, valueClass);
        this.values = values;
    }

    @Override
    public Map<Integer, T> getValues() {
        return Collections.unmodifiableMap(values);
    }

    @Override
    public T getValue(int index) {
        return this.values.get(index);
    }

    @Override
    public T getValue() {
        return getValue(0);
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
    public void setValues(List<T> values) {
        this.values.clear();
        for (int id = 0; id < values.size(); id++) {
            this.values.put(id, values.get(id));
        }
        if(client != null) {
            for (ServerObjectInstance server : client.getServerObjectResolver().getRegisteredServers()) {
                if (numericObservationResolver.shouldNotify(server.getShortServerId())) {
                    notifyObservers(server.getShortServerId());
                }
            }
        }
    }

    @Override
    public void setValue(ResourceValue<?> value) {
        values.put(value.id, (T) value);
        if(client != null) {
            for (ServerObjectInstance server : client.getServerObjectResolver().getRegisteredServers()) {
                if (numericObservationResolver.shouldNotify(server.shortServerId.getValue().value)) {
                    notifyObservers(server.shortServerId.getValue().value);
                }
            }
        }
    }

}
