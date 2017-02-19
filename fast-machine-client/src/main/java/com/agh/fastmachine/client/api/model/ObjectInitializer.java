package com.agh.fastmachine.client.api.model;

import com.agh.fastmachine.client.api.Client;
import com.agh.fastmachine.client.api.model.builtin.AccessControlObjectBase;
import com.agh.fastmachine.client.api.model.builtin.AccessControlObjectInstance;
import com.agh.fastmachine.client.api.model.builtin.SecurityObjectBase;
import com.agh.fastmachine.client.api.model.builtin.SecurityObjectInstance;
import com.agh.fastmachine.client.internal.attribute.AttributesFactory;
import com.agh.fastmachine.core.api.model.Attributes;
import com.agh.fastmachine.core.api.model.resourcevalue.IntegerResourceValue;
import com.agh.fastmachine.core.api.model.Operations;
import com.agh.fastmachine.core.internal.model.ObjectResourceModel;
import org.eclipse.californium.core.server.resources.Resource;

public class ObjectInitializer {
    private Client client;
    private AccessControlObjectBase accessControlBase;

    public ObjectInitializer(Client client) {
        this.client = client;
        accessControlBase = (AccessControlObjectBase) client.getObjectBaseMap().get(2);
    }

    public void initializeObject(ObjectBase<?> objectBase) {
        Lwm2mNodeCoapResource resource = new Lwm2mNodeCoapResource(objectBase);
        client.getClientResource().add(resource);

        objectBase.setClient(client);
        objectBase.setAccessControlObjectInstance(getNewACO(objectBase.getId(), 65535, 65535));

        for (ObjectInstance objectInstance : objectBase.getObjectInstances().values()) {
            objectInstance.gatherResources();
            initializeInstance(objectInstance, 66535);
        }

        if (objectBase.getId() == 0) {
            initializeSecurityObject((SecurityObjectBase) objectBase);
        }
    }

    private void initializeSecurityObject(SecurityObjectBase securityObject) {
        for (SecurityObjectInstance security : securityObject.getObjectInstances().values()) {
            client.getServerSecurityMap().put(security.shortServerId.getValue().value, security);
        }
    }

    public void initializeInstance(ObjectInstance instance, int serverId) {
        instance.setAccessControlObjectInstance(getNewACO(instance.parent().getId(), instance.getId(), serverId));
        initializeCoapResources(instance);
        instance.setClient(client);
    }

    public void initAttr(ObjectInstance instance, int serverId) {
        instance.writeAttributes.put(serverId, AttributesFactory.create(instance));
        for (ObjectResourceModel<?> objectResourceModel : instance.getResources().values()) {
            ObjectResource objectResource = (ObjectResource) objectResourceModel;
            objectResource.writeAttributes.put(serverId, AttributesFactory.create(objectResource));
        }
    }

    public AccessControlObjectInstance getNewACO(int objectId, int instanceId, Integer serverId) {
        AccessControlObjectInstance aco = accessControlBase.getNewInstance();
        ((ObjectInstance) aco).setAccessControlObjectInstance(new AccessControlObjectInstance(-1));
        ((ObjectInstance) aco).gatherResources();
        aco.objectId.setValue(new IntegerResourceValue(objectId));
        aco.instanceId.setValue(new IntegerResourceValue(instanceId));
        aco.accessControlOwner.setValue(new IntegerResourceValue(serverId));
        if (instanceId == 65535)
            aco.accessControlList.setValue(new IntegerResourceValue(Operations.CREATE, 66535));
        ((ObjectInstance) aco).setClient(client);
        initializeCoapResources(aco);
        return aco;
    }

    private void initializeCoapResources(ObjectInstance instance) {
        Lwm2mNodeCoapResource instanceResource = new Lwm2mNodeCoapResource(instance);
        instanceResource.setParent(instance.parent().getCoapResource());
        for (ObjectResource<?> resource : instance.getObjectResources().values()) {
            resource.setClient(client);
            resource.initObservationResolver();
            Lwm2mNodeCoapResource coapResource = new Lwm2mNodeCoapResource(resource);
            coapResource.setParent(instanceResource);
            instanceResource.add(coapResource);
        }
        client.getClientResource().getChild(String.valueOf(instance.parent().getId())).add(instanceResource);
    }

    public void cleanupInstance(ObjectInstance instance) {
        Resource objectBase = client.getClientResource().getChild(String.valueOf(instance.parent().getId()));
        objectBase.delete(objectBase.getChild(String.valueOf(instance.getId())));
        Resource aco = client.getClientResource().getChild("2");
        aco.delete(aco.getChild(String.valueOf(instance.getAccessControlObjectInstance().getId())));
        ((ObjectBase) accessControlBase).delete(instance.getAccessControlObjectInstance().getId());
    }

    public void initializeWriteAttributes(Integer shortServerId) {
        for (ObjectBase<?> objectBase : client.getObjectBaseMap().values()) {
            objectBase.writeAttributes.put(shortServerId, AttributesFactory.create(objectBase));
            for (ObjectInstance objectInstance : objectBase.getObjectInstances().values()) {
                objectInstance.writeAttributes.put(shortServerId, AttributesFactory.create(objectInstance));
                for (ObjectResourceModel<?> objectResourceModel : objectInstance.getResources().values()) {
                    ObjectResource objectResource = (ObjectResource) objectResourceModel;
                    objectResource.writeAttributes.put(shortServerId, AttributesFactory.create(objectResource));
                }
            }
        }
    }
}
