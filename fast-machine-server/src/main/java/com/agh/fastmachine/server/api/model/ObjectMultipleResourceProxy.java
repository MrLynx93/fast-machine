package com.agh.fastmachine.server.api.model;

import com.agh.fastmachine.core.internal.model.ObjectMultipleResourceModel;
import com.agh.fastmachine.core.api.model.resourcevalue.ResourceValue;
import com.agh.fastmachine.core.internal.visitor.NodeVisitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectMultipleResourceProxy<T extends ResourceValue<?>> extends ObjectResourceProxy<T> implements ObjectMultipleResourceModel<T> {
    Map<Integer, T> values = new HashMap<>();

    public ObjectMultipleResourceProxy(int id, Map<Integer, T> values, Class<T> valueClass) {
        super(id, values.get(0), valueClass);
        this.values = values;
        internal = new Internal();
    }

    @Override
    public boolean isMultiple() {
        return true;
    }

    @Override
    public T getValue() {
        return values.get(0);
    }

    public T getValue(int id) {
        return values.get(id);
    }

    public Map<Integer, T> getValues() {
        return values;
    }

    @Override
    public void setValue(ResourceValue<?> value) {
        this.values.put(value.id, (T) value);
        this.isChanged = true;
    }

    public void setValue(T value, int id) {
        this.values.put(id, value);
        this.isChanged = true;
    }

    @Override
    public void setValues(List<T> values) {
        this.values.clear();
        for (int id = 0; id < values.size(); id++) {
            this.values.put(id, values.get(id));
        }
        this.isChanged = true;
    }

    /// INTERNAL ///

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

}
