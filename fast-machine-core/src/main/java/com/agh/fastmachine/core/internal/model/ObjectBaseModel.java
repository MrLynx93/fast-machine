package com.agh.fastmachine.core.internal.model;

import java.util.Map;

public interface ObjectBaseModel<T extends ObjectInstanceModel> extends ObjectNodeModel {
    Map<Integer, T> getObjectInstances();
    T getInstance(int id);
}
