package com.agh.fastmachine.core.api.model.resourcevalue;


import com.agh.fastmachine.core.internal.visitor.VisitableNode;

public abstract class ResourceValue<T> implements VisitableNode {
    public int id;
    public T value;

    public ResourceValue() {
    }

    public int getId() {
        return id;
    }

    public T getValue() {
        return value;
    }

    public ResourceValue(T value, int id) {
        this.id = id;
        this.value = value;
    }

    public ResourceValue(T value) {
        this.value = value;
        this.id = 0;
    }


    public abstract void setValue(String value);

}

