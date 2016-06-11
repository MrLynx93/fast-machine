package com.agh.fastmachine.client.internal.visitor.access;


import com.agh.fastmachine.client.api.model.*;
import com.agh.fastmachine.client.internal.parser.ClientObjectFactory;
import com.agh.fastmachine.client.internal.visitor.ObjectNodeVisitor;
import com.agh.fastmachine.core.internal.model.ObjectInstanceModel;
import com.agh.fastmachine.core.internal.model.ObjectResourceModel;
import com.agh.fastmachine.core.api.model.resourcevalue.ResourceValue;
import com.agh.fastmachine.core.api.model.Operations;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ReadAccessFilterVisitor implements ObjectNodeVisitor {
    private ClientObjectFactory objectFactory = new ClientObjectFactory();
    private ObjectResource<?> resource;
    private ObjectInstance instance;
    private ObjectBase<?> object;

    public AbstractLwm2mNode getFilteredNode() {
        if (object != null) {
            return object;
        }
        if (instance != null) {
            return instance;
        }
        return resource;
    }

    @Override
    public <T extends ObjectInstance> void visit(ObjectBase<T> objectBase) {
        object = null;
        Map<Integer, ObjectInstanceModel> instances = new TreeMap<>();
        for (ObjectInstance objectInstance : objectBase.getObjectInstances().values()) {
            objectInstance.accept(this);
            instances.put(instance.getId(), instance);
        }
        object = (ObjectBase<?>) objectFactory.createObjectBase(instances);
    }

    @Override
    public void visit(ObjectInstance objectInstance) {
        instance = null;

        Map<Integer, ObjectResourceModel<?>> resources = new HashMap<>();
        for (ObjectResource<?> objectResource : objectInstance.getObjectResources().values()) {
            objectResource.accept(this);
            if (resource != null) {
                resources.put(resource.getId(), resource);
            }
        }
        instance = (ObjectInstance) objectFactory.createObjectInstance(objectInstance.getId(), resources);
    }

    @Override
    public <T extends ResourceValue<?>> void visit(ObjectResource<T> objectResource) {
        resource = null;
        if ((objectResource.getAllowedOperations() & Operations.READ) == Operations.READ) {
            resource = objectResource;
        }
    }

    @Override
    public <T extends ResourceValue<?>> void visit(ObjectMultipleResource<T> objectMultipleResource) {
        resource = null;
        if ((objectMultipleResource.getAllowedOperations() & Operations.READ) == Operations.READ) {
            resource = objectMultipleResource;
        }
    }


}


