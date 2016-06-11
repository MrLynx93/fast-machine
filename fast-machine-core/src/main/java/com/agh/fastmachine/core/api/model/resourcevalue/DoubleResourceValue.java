package com.agh.fastmachine.core.api.model.resourcevalue;

import com.agh.fastmachine.core.internal.visitor.NodeVisitor;

public class DoubleResourceValue extends ResourceValue<Double> {

    public DoubleResourceValue() {
    }

    public DoubleResourceValue(Double value, int id) {
        super(value, id);
    }

    public DoubleResourceValue(Double value) {
        super(value);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void setValue(String value) {
        this.value = Double.valueOf(value);
    }
}
