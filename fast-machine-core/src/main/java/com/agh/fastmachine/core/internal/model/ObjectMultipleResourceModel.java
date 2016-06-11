package com.agh.fastmachine.core.internal.model;

import com.agh.fastmachine.core.api.model.resourcevalue.ResourceValue;

import java.util.List;
import java.util.Map;

public interface ObjectMultipleResourceModel<T extends ResourceValue<?>> extends ObjectResourceModel<T> {
    Map<Integer, T> getValues();
    void setValues(List<T> values);
}
