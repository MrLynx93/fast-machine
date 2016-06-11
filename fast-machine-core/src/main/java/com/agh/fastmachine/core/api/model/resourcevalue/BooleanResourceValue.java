package com.agh.fastmachine.core.api.model.resourcevalue;

import com.agh.fastmachine.core.internal.visitor.NodeVisitor;

public class BooleanResourceValue extends ResourceValue<Boolean> {

    public BooleanResourceValue(Boolean value, int id) {
        super(value, id);
    }

    public BooleanResourceValue(Boolean value) {
        super(value);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public BooleanResourceValue() {
    }

    @Override
    public void setValue(String value) {
        this.value = Boolean.valueOf(value);
    }
}
