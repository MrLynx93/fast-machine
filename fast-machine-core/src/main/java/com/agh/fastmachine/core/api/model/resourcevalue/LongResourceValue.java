package com.agh.fastmachine.core.api.model.resourcevalue;


import com.agh.fastmachine.core.internal.visitor.NodeVisitor;

public class LongResourceValue extends ResourceValue<Long> {

    public LongResourceValue() {
    }

    public LongResourceValue(Long value, int id) {
        super(value, id);
    }

    public LongResourceValue(Long value) {
        super(value);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }


    @Override
    public void setValue(String value) {
        Long.valueOf(value);
    }
}
