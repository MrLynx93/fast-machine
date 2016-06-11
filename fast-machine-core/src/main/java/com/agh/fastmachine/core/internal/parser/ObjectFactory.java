package com.agh.fastmachine.core.internal.parser;

import com.agh.fastmachine.core.internal.model.ObjectBaseModel;
import com.agh.fastmachine.core.internal.model.ObjectInstanceModel;
import com.agh.fastmachine.core.internal.model.ObjectMultipleResourceModel;
import com.agh.fastmachine.core.internal.model.ObjectResourceModel;
import com.agh.fastmachine.core.api.model.resourcevalue.ResourceValue;

import java.util.Map;

public interface ObjectFactory {
    <T extends ObjectInstanceModel> ObjectBaseModel<T> createObjectBase(Map<Integer, T> instances);
    ObjectInstanceModel createObjectInstance(int id, Map<Integer, ObjectResourceModel<?>> resources);
    <T extends ResourceValue<?>> ObjectResourceModel<T> createObjectResource(int id, T value, Class<T> valueClass);
    <T extends ResourceValue<?>> ObjectMultipleResourceModel<T> createObjectMultipleResource(int id, Map<Integer, T> values, Class<T> valueClass);
}
