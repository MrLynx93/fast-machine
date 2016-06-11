package com.agh.fastmachine.core.internal.model;


import java.util.Map;

public interface ObjectInstanceModel extends ObjectNodeModel {
    public Map<Integer, ? extends ObjectResourceModel<?>> getResources();
    public ObjectResourceModel<?> getResource(int id);
}
