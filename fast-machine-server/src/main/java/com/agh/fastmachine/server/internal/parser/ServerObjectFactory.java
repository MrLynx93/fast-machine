package com.agh.fastmachine.server.internal.parser;

import com.agh.fastmachine.core.internal.model.ObjectBaseModel;
import com.agh.fastmachine.core.internal.model.ObjectInstanceModel;
import com.agh.fastmachine.core.internal.model.ObjectMultipleResourceModel;
import com.agh.fastmachine.core.internal.model.ObjectResourceModel;
import com.agh.fastmachine.core.api.model.resourcevalue.ResourceValue;
import com.agh.fastmachine.core.internal.parser.ObjectFactory;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectMultipleResourceProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;

import java.util.Map;

public class ServerObjectFactory implements ObjectFactory {

    @Override
    public <T extends ObjectInstanceModel> ObjectBaseModel<T> createObjectBase(Map<Integer, T> instances) {
        return new ObjectBaseProxy(-1, instances);
    }

    @Override
    public ObjectInstanceModel createObjectInstance(int id, Map<Integer, ObjectResourceModel<?>> resources) {
        return new ObjectInstanceProxy(id, resources) {};
    }

    @Override
    public <T extends ResourceValue<?>> ObjectResourceModel<T> createObjectResource(int id, T value, Class<T> valueClass) {
        return new ObjectResourceProxy<>(id, value, valueClass);
    }

    @Override
    public <T extends ResourceValue<?>> ObjectMultipleResourceModel<T> createObjectMultipleResource(int id, Map<Integer, T> values, Class<T> valueClass) {
        return new ObjectMultipleResourceProxy<>(id, values, valueClass);
    }
}
