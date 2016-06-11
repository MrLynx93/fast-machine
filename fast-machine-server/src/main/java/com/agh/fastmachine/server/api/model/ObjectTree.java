package com.agh.fastmachine.server.api.model;

import com.agh.fastmachine.server.api.ClientProxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ObjectTree {
    private final Map<Integer, ObjectBaseProxy<?>> objects;
    private final Map<Integer, Class<? extends ObjectInstanceProxy>> instanceClasses;
    public ClientProxy clientProxy;

    public ObjectTree(Map<Integer, ObjectBaseProxy<?>> objects, Map<Integer, Class<? extends ObjectInstanceProxy>> instanceClasses) {
        this.objects = objects;
        this.instanceClasses = instanceClasses;
    }

    public <T extends ObjectInstanceProxy> ObjectBaseProxy<T> getObjectForType(Class<T> instanceClass) {
        for (Map.Entry<Integer, Class<? extends ObjectInstanceProxy>> classEntry : instanceClasses.entrySet()) {
            if (classEntry.getValue().equals(instanceClass)) {
                return (ObjectBaseProxy<T>) objects.get(classEntry.getKey());
            }
        }
        return null;
    }

    public ObjectBaseProxy<?> getObjectForId(int id) {
        return objects.get(id);
    }

    public List<ObjectBaseProxy<?>> getObjects() {
        return new ArrayList<ObjectBaseProxy<?>>(objects.values());
    }

}
