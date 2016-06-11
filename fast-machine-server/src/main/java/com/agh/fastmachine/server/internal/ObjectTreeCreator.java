package com.agh.fastmachine.server.internal;

import com.agh.fastmachine.core.api.model.Attributes;
import com.agh.fastmachine.core.api.model.annotation.Lwm2mResource;
import com.agh.fastmachine.core.api.model.resourcevalue.FloatResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.IntegerResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.LongResourceValue;
import com.agh.fastmachine.server.api.model.ObjectBaseProxy;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;
import com.agh.fastmachine.server.api.model.ObjectTree;
import com.agh.fastmachine.server.internal.client.ClientProxyImpl;
import com.agh.fastmachine.server.internal.service.registrationinfo.RegistrationObjectInfo;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public class ObjectTreeCreator {

    public ObjectTree createObjectTree(ClientProxyImpl clientProxy, List<RegistrationObjectInfo> objectInfoList) {
        Map<Integer, Class<? extends ObjectInstanceProxy>> instanceClasses = clientProxy.getServer().getConfiguration().getSupportedObjects();
        Map<Integer, ObjectBaseProxy<?>> objects = new HashMap<>();

        for (RegistrationObjectInfo objectInfo : objectInfoList) {
            String url = objectInfo.url;
            Integer objectId = objectInfo.objectId;
            Integer instanceId = objectInfo.instanceId;

            ObjectBaseProxy<?> object = objects.get(objectId);
            if (object == null) {
                object = new ObjectBaseProxy<>(objectId);
                initializeObject(object, url, clientProxy);
                objects.put(objectId, object);
            }

            try {
                Class<? extends ObjectInstanceProxy> instanceClass = instanceClasses.get(objectId);
                ObjectInstanceProxy instance = instanceClass.getConstructor(int.class).newInstance(instanceId);
                initializeInstance(instance, clientProxy, url);
                for (ObjectResourceProxy<?> resource : instance.getResources().values()) {
                    initializeResource(resource, resource.getValueType(), clientProxy);
                }
                instance.internal().setObject(object);
                object.addInstance(instance);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return new ObjectTree(objects, instanceClasses);
    }

    public <T extends ObjectInstanceProxy> void connectToRemoteClient(T instance, int instanceId, ClientProxyImpl clientProxy) {
        ObjectBaseProxy objectForInstance = clientProxy.getObjectTree().getObjectForType(instance.getClass());
        instance.internal().setObject(objectForInstance);
        instance.internal().setId(instanceId);

        initializeInstance(instance, clientProxy, null);
        makeResourcesRemote(instance, clientProxy);
    }

    /// INTERNAL ///

    private <T extends ObjectInstanceProxy> void makeResourcesRemote(T instance, ClientProxyImpl clientProxy) {
        try {
            for (Field field : getResourceFields(instance.getClass())) {
                ObjectResourceProxy<?> resource = (ObjectResourceProxy) field.get(instance);
                Class<?> resourceValueClass = getResourceValueClass(field);
                initializeResource(resource, resourceValueClass, clientProxy);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeObject(ObjectBaseProxy<?> object, String url, ClientProxyImpl clientProxy) {
        object.internal().updateAttributes(new Attributes(false));
        object.internal().setTransportLayer(clientProxy.getTransportLayer());
        object.internal().setClientProxy(clientProxy);
        object.internal().setSupported(true);
        if (url != null && !url.isEmpty()) {
            object.internal().setPath(url);
        }
    }


    private void initializeInstance(ObjectInstanceProxy instance, ClientProxyImpl clientProxy, String url) {
        instance.internal().updateAttributes(new Attributes(false));
        instance.internal().setTransportLayer(clientProxy.getTransportLayer());
        instance.internal().setClientProxy(clientProxy);
        instance.internal().setSupported(true);
        instance.internal().setUrl(url);
    }

    private void initializeResource(ObjectResourceProxy<?> resource, Class<?> resourceValueClass, ClientProxyImpl clientProxy) {
        resource.internal().updateAttributes(new Attributes(isNumericValue(resourceValueClass)));
        resource.internal().setTransportLayer(clientProxy.getTransportLayer());
        resource.internal().setClientProxy(clientProxy);
        resource.internal().setSupported(true);
    }

    private Collection<Field> getResourceFields(Class<? extends ObjectInstanceProxy> instanceClass) {
        ArrayList<Field> resources = new ArrayList<>();
        for (Field field : instanceClass.getFields()) {
            if (field.getAnnotation(Lwm2mResource.class) != null) {
                resources.add(field);
            }
        }
        return resources;
    }

    private Class<?> getResourceValueClass(Field field) {
        ParameterizedType parametrizedType = (ParameterizedType) field.getGenericType();
        return (Class<?>) parametrizedType.getActualTypeArguments()[0];
    }

    private boolean isNumericValue(Class<?> valueClass) {
        return valueClass.equals(IntegerResourceValue.class) || valueClass.equals(FloatResourceValue.class) || valueClass.equals(LongResourceValue.class);
    }

}
