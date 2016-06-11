package com.agh.fastmachine.core.api.model.resourcevalue;


import com.agh.fastmachine.core.internal.visitor.NodeVisitor;

public class IntegerResourceValue extends ResourceValue<Integer> {

    public IntegerResourceValue() {
    }

    public IntegerResourceValue(Integer value, int id) {
        super(value, id);
    }

    public IntegerResourceValue(Integer value) {
        super(value);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void setValue(String value) {
        this.value = Integer.valueOf(value);
    }

    //    public IntegerResourceValue(int id) {
//        super(id);
//    }
}
