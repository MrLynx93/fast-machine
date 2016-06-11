package com.agh.fastmachine.client.internal.parser;

import com.agh.fastmachine.client.api.model.ObjectBase;
import com.agh.fastmachine.client.api.model.ObjectInstance;
import com.agh.fastmachine.client.api.model.ObjectMultipleResource;
import com.agh.fastmachine.client.api.model.ObjectResource;
import com.agh.fastmachine.core.api.model.annotation.Lwm2mObject;
import com.agh.fastmachine.core.internal.model.ObjectBaseModel;
import com.agh.fastmachine.core.internal.model.ObjectInstanceModel;
import com.agh.fastmachine.core.internal.model.ObjectMultipleResourceModel;
import com.agh.fastmachine.core.internal.model.ObjectResourceModel;
import com.agh.fastmachine.core.api.model.resourcevalue.ResourceValue;
import com.agh.fastmachine.core.internal.parser.ObjectFactory;

import java.util.Map;
import java.util.TreeMap;

public class ClientObjectFactory implements ObjectFactory {

    @Override
    public <T extends ObjectInstanceModel> ObjectBaseModel<T> createObjectBase(Map<Integer, T> instances) {
        return new ObjectBaseImpl((TreeMap) instances);
    }

    @Override
    public ObjectInstanceModel createObjectInstance(int id, Map<Integer, ObjectResourceModel<?>> resources) {
        return new ObjectInstanceImpl(id, resources);
    }

    @Override
    public <T extends ResourceValue<?>> ObjectResourceModel<T> createObjectResource(int id, T value, Class<T> valueClass) {
        return new ObjectResource<>(id, value, valueClass);
    }

    @Override
    public <T extends ResourceValue<?>> ObjectMultipleResourceModel<T> createObjectMultipleResource(int id, Map<Integer, T> values, Class<T> valueClass) {
        return new ObjectMultipleResource<>(id, values, valueClass);
    }

    @Lwm2mObject(id = 0)
    private class ObjectBaseImpl<T extends ObjectInstance> extends ObjectBase<T> {

        public ObjectBaseImpl(TreeMap<Integer, T> instances) {
            super(null);
            objectInstances = instances;
        }
    }

    private class ObjectInstanceImpl extends ObjectInstance {

        private final Map<Integer, ObjectResourceModel<?>> resources;

        public ObjectInstanceImpl(int id, Map<Integer, ObjectResourceModel<?>> resources) {
            super(id);
            this.resources = resources;
        }

        @Override
        public Map<Integer, ObjectResourceModel<?>> getResources() {
            return resources;
        }
    }
}
