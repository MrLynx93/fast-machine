package com.agh.fastmachine.core.api.model.resourcevalue;


import com.agh.fastmachine.core.internal.visitor.NodeVisitor;

import java.nio.charset.Charset;

public class OpaqueResourceValue extends ResourceValue<byte[]> {

    public OpaqueResourceValue() {

    }

    public OpaqueResourceValue(byte[] value, int id) {
        super(value, id);
    }

    public OpaqueResourceValue(byte[] value) {
        super(value);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void setValue(String value) {
        this.value = value.getBytes(Charset.forName("US-ASCII"));
    }
}
