package com.agh.fastmachine.core.api.model.resourcevalue;

import com.agh.fastmachine.core.internal.visitor.NodeVisitor;

public class FloatResourceValue extends ResourceValue<Float> {

    public FloatResourceValue() {
    }

    public FloatResourceValue(Float value, int id) {
        super(value, id);
    }

    public FloatResourceValue(Float value) {
        super(value);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void setValue(String value) {
        this.value = Float.valueOf(value);
    }
}
