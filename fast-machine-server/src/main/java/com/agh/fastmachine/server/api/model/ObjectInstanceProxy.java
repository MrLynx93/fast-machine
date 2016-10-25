package com.agh.fastmachine.server.api.model;

import com.agh.fastmachine.core.api.model.annotation.Lwm2mResource;
import com.agh.fastmachine.core.internal.model.ObjectInstanceModel;
import com.agh.fastmachine.core.internal.model.ObjectResourceModel;
import com.agh.fastmachine.core.api.model.resourcevalue.FloatResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.IntegerResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.LongResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.ResourceValue;
import com.agh.fastmachine.server.internal.parser.ServerObjectFactory;
import com.agh.fastmachine.core.internal.visitor.NodeVisitor;
import com.agh.fastmachine.server.api.exception.ObjectDeletedException;
import com.agh.fastmachine.server.api.listener.ObservationListener;
import com.agh.fastmachine.server.internal.transport.LWM2M;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public abstract class ObjectInstanceProxy extends ObjectNodeProxy<ObjectInstanceProxy.Internal> implements ObjectInstanceModel {
    private Map<Integer, ObjectResourceProxy<?>> resources = new HashMap<>();
    private ObjectInstanceProxy clientInstance;
    private ObjectBaseProxy object;
    private boolean isDeleted;
    private String url;

    public ObjectInstanceProxy() {
        this(0);
        this.internal = new Internal();
    }

    public ObjectInstanceProxy(int id) {
        super(id);
        this.internal = new Internal();
        try {
            for (Field field : getClass().getFields()) {
                if (field.getAnnotation(Lwm2mResource.class) != null) {
                    ObjectResourceProxy<?> resource = createResource(field);
                    resource.internal().setInstance(this);
                    this.internal().addResource(resource);
                    field.set(this, resource);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        internal = new Internal();
    }

    public ObjectInstanceProxy(int id, Map<Integer, ObjectResourceModel<?>> resources) {
        super(id);
        this.resources = (Map) resources;
        this.internal = new Internal();
    }

    @Override
    public LWM2M.Path getPath() {
        return LWM2M.Path.of(getObject().getId(), id);
    }

    public void read() {
        if (isDeleted) {
            throw new ObjectDeletedException();
        }
        transport.read(this);
    }

    public void write() {
        if (isDeleted) {
            throw new ObjectDeletedException();
        }
        if (isChanged()) {
            Map<Integer, ObjectResourceModel<?>> modifiedResources = findModifiedResources();
            ServerObjectFactory factory = new ServerObjectFactory();

            ObjectInstanceProxy instance = (ObjectInstanceProxy) factory.createObjectInstance(this.getId(), modifiedResources);
            instance.internal().setObject(this.object);

            transport.write(this);

            for (ObjectResourceModel<?> resource : modifiedResources.values()) {
                ((ObjectResourceProxy<?>) resource).internal().setChanged(false);
            }
        }
    }

    public void discover() {
        if (isDeleted) {
            throw new ObjectDeletedException();
        }
        transport.discover(this);
    }

    public void writeAttributes() {
        if (isDeleted) {
            throw new ObjectDeletedException();
        }
        transport.writeAttributes(this);
    }

    public void delete() {
        if (isDeleted) {
            throw new ObjectDeletedException();
        }
        transport.delete(this);
        object.getObjectInstances().remove(this.getId());
//        object.getObjectInternalInstances().remove(this.getId());

        for (ObjectResourceProxy<?> objectResourceProxy : resources.values()) {
            objectResourceProxy.isDeleted = true;
        }
        this.isDeleted = true;
    }

    public <T extends ObjectInstanceProxy> void observe(ObservationListener<T> listener) {
        if (isDeleted) {
            throw new ObjectDeletedException();
        }
        transport.observe(this, listener);
    }

    public void cancelObservation() {
        if (isDeleted) {
            throw new ObjectDeletedException();
        }
        transport.cancelObserve(this);
    }

    public boolean isChanged() {
        if (isDeleted) {
            throw new ObjectDeletedException();
        }
        for (ObjectResourceProxy<?> objectResourceProxy : resources.values()) {
            if (objectResourceProxy.isChanged()) {
                return true;
            }
        }
        return false;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    @Override
    public Map<Integer, ? extends ObjectResourceProxy<?>> getResources() {
        return resources;
    }

    @Override
    public ObjectResourceProxy<?> getResource(int id) {
        return resources.get(id);
    }

    public ObjectBaseProxy getObject() {
        return object;
    }

    /// INTERNAL ///

    @SuppressWarnings("unchecked")
    private ObjectResourceProxy<?> createResource(Field field) throws IllegalAccessException, InstantiationException {
        Lwm2mResource annotation = field.getAnnotation(Lwm2mResource.class);
        Class<?> resourceValueClass = getResourceValueClass(field);
        int resourceId = annotation.id();

        ObjectResourceProxy<?> resource;

        if (isMultipleResource(field)) {
            resource = new ObjectMultipleResourceProxy(resourceId, new TreeMap(), resourceValueClass);
        } else {
            ResourceValue<?> resourceValue = (ResourceValue<?>) resourceValueClass.newInstance();
            resourceValue.id = 0;
            resourceValue.value = null;
            resource = new ObjectResourceProxy(resourceId, resourceValue, resourceValueClass);
        }
        return resource;
    }

    private boolean isMultipleResource(Field field) {
        return ObjectMultipleResourceProxy.class.equals(field.getType());
    }

    private Class<?> getResourceValueClass(Field field) {
        ParameterizedType parametrizedType = (ParameterizedType) field.getGenericType();
        return (Class<?>) parametrizedType.getActualTypeArguments()[0];
    }

    private boolean isNumericValue(Class<?> valueClass) {
        return valueClass.equals(IntegerResourceValue.class) || valueClass.equals(FloatResourceValue.class) || valueClass.equals(LongResourceValue.class);
    }

    private Map<Integer, ObjectResourceModel<?>> findModifiedResources() {
        Map<Integer, ObjectResourceModel<?>> modifiedResources = new HashMap<>();
        for (ObjectResourceProxy<?> resource : resources.values()) {
            if (resource.isChanged()) {
                modifiedResources.put(resource.getId(), resource);
            }
        }
        return modifiedResources;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public class Internal extends ObjectNodeProxy.Internal {

        public void setId(int id) {
            ObjectInstanceProxy.this.id = id;
        }

        public void setClientInstance(ObjectInstanceProxy clientInstance) {
            ObjectInstanceProxy.this.clientInstance = clientInstance;
        }

        public ObjectInstanceProxy getClientInstance() {
            return clientInstance;
        }

        public void setObject(ObjectBaseProxy object) {
            ObjectInstanceProxy.this.object = object;
        }

        public void addResource(ObjectResourceProxy<?> resource) {
            resources.put(resource.getId(), resource);
        }

        public void setUrl(String url) {
            ObjectInstanceProxy.this.url = url;
        }

        public String getUrl() {
            return url;
        }

        public void update(ObjectInstanceProxy newInstance) {
            for (Map.Entry<Integer, ? extends ObjectResourceProxy<?>> resourceEntry : newInstance.getResources().entrySet()) {
                int resourceId = resourceEntry.getKey();
                ResourceValue<?> newValue = resourceEntry.getValue().getValue();
                resources.get(resourceId).internal().update(newValue);
            }
            setChanged(false);
        }

        public void setChanged(boolean changed) {
            for (ObjectResourceProxy<?> resource : resources.values()) {
                resource.internal().setChanged(changed);
            }
        }

    }


}
