package com.agh.fastmachine.core.internal.model;

import com.agh.fastmachine.core.api.model.resourcevalue.ResourceValue;

public interface ObjectResourceModel<T extends ResourceValue<?>> extends ObjectNodeModel {
    public T getValue();
    public Class<T> getValueType();
    public ObjectInstanceModel getInstance();
}
