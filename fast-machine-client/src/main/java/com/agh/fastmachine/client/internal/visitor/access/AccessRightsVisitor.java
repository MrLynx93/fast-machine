package com.agh.fastmachine.client.internal.visitor.access;

import com.agh.fastmachine.client.api.model.*;
import com.agh.fastmachine.client.api.model.builtin.AccessControlObjectInstance;
import com.agh.fastmachine.client.internal.ServerObjectResolver;
import com.agh.fastmachine.client.internal.visitor.ObjectNodeVisitor;
import com.agh.fastmachine.client.internal.parser.ClientObjectFactory;
import com.agh.fastmachine.core.api.model.resourcevalue.IntegerResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.ResourceValue;
import com.agh.fastmachine.core.api.model.Operations;

import java.util.TreeMap;

public class AccessRightsVisitor implements ObjectNodeVisitor {
    private ServerObjectResolver serverObjectResolver;

    private int shortServerId;
    private int operationToCheck;
    private AbstractLwm2mNode resultNode;

    public AccessRightsVisitor(int shortServerId, int operationToCheck, AbstractLwm2mNode resultNode) {
        this.serverObjectResolver = resultNode.getClient().getServerObjectResolver();
        this.shortServerId = shortServerId;
        this.operationToCheck = operationToCheck;
        this.resultNode = resultNode;
    }

    public AbstractLwm2mNode getResultNode() {
        return resultNode;
    }

    @Override
    public <T extends ObjectInstance> void visit(ObjectBase<T> objectBase) {
        if (operationToCheck == Operations.CREATE) {
            IntegerResourceValue value = objectBase.getAccessControlObjectInstance().accessControlList.getValue(shortServerId);
            if (value != null && (value.value & Operations.CREATE) == Operations.CREATE)
                resultNode = objectBase;
            else resultNode = null;
            return;
        }
        TreeMap<Integer, T> accessibleInstances = new TreeMap<>();
        for (T instance : objectBase.getObjectInstances().values()) {
            if (hasAccessRights(shortServerId, instance.getAccessControlObjectInstance()))
                accessibleInstances.put(instance.getId(), instance);
        }

        resultNode = (AbstractLwm2mNode) new ClientObjectFactory().createObjectBase(accessibleInstances);
    }

    @Override
    public void visit(ObjectInstance instance) {
        if (hasAccessRights(shortServerId, instance.getAccessControlObjectInstance()))
            resultNode = instance;
        else
            resultNode = null;
    }

    @Override
    public <T extends ResourceValue<?>> void visit(ObjectResource<T> resource) {
        visitResource(resource);
    }

    @Override
    public <T extends ResourceValue<?>> void visit(ObjectMultipleResource<T> multipleResource) {
        visitResource(multipleResource);
    }

    private <T extends ResourceValue<?>> void visitResource(ObjectResource<T> resource) {
        if (hasAccessRights(shortServerId, resource.parent().getAccessControlObjectInstance())) {
            resultNode = resource;
        }
        else {
            resultNode = null;
        }
    }

    private boolean hasAccessRights(int shortServerId, AccessControlObjectInstance instanceAccessControl) {
        if (thisIsOnlyServerRegistered(shortServerId)) {
            return true;
        }
        if (accessControlListIsEmpty(instanceAccessControl) && serverIsAccessControlOwner(shortServerId, instanceAccessControl)) {
            return true;
        }
        if (checkServerInAccessControlList(shortServerId, instanceAccessControl)) {
            return true;
        }
        return checkDefaultRights(instanceAccessControl);
    }

    private boolean checkDefaultRights(AccessControlObjectInstance instanceAccessControl) {
        IntegerResourceValue value = instanceAccessControl.accessControlList.getValue(0);
        return value != null && (value.value & operationToCheck) == operationToCheck;
    }

    private boolean checkServerInAccessControlList(int shortServerId, AccessControlObjectInstance instanceAccessControl) {
        IntegerResourceValue value = instanceAccessControl.accessControlList.getValue(shortServerId);
        return value != null && (value.value & operationToCheck) == operationToCheck;
    }

    private boolean accessControlListIsEmpty(AccessControlObjectInstance instanceAccessControl) {
        return instanceAccessControl.accessControlList.getValues().size() == 0;
    }

    private boolean serverIsAccessControlOwner(int shortServerId, AccessControlObjectInstance instanceAccessControl) {
        return shortServerId == instanceAccessControl.accessControlOwner.getValue().value;
    }

    private boolean thisIsOnlyServerRegistered(int shortServerId) {
        return serverObjectResolver.getNumberOfRegisteredServers() == 1 && serverObjectResolver.resolveServerObject(shortServerId) != null;
    }

}
