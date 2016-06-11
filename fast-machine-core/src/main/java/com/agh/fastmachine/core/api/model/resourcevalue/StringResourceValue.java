package com.agh.fastmachine.core.api.model.resourcevalue;


import com.agh.fastmachine.core.internal.visitor.NodeVisitor;

public class StringResourceValue extends ResourceValue<String> {

    public StringResourceValue() {
    }

    public StringResourceValue(String value, int id) {
        super(value, id);
    }

    public StringResourceValue(String value) {
        super(value);
    }


    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }
}
