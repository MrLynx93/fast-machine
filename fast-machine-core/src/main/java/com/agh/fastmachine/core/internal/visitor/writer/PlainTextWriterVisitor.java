package com.agh.fastmachine.core.internal.visitor.writer;

import com.agh.fastmachine.core.internal.model.ObjectResourceModel;
import com.agh.fastmachine.core.api.model.resourcevalue.*;

import java.nio.charset.Charset;

public class PlainTextWriterVisitor extends AbstractWriterVisitor {

    private byte[] bytes;

    @Override
    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public void visit(ObjectResourceModel objectResource) {
        if (objectResource.getValue() != null)
            objectResource.getValue().accept(this);
    }

    @Override
    public void visit(StringResourceValue resourceValue) {
        bytes = resourceValue.value.getBytes(Charset.forName("UTF-8"));
    }

    @Override
    public void visit(IntegerResourceValue resourceValue) {
        setAsciiBytes(resourceValue);
    }

    @Override
    public void visit(BooleanResourceValue resourceValue) {
        bytes = resourceValue.value ? new byte[]{49} : new byte[]{48}; //ascii 1 and 0
    }

    @Override
    public void visit(OpaqueResourceValue resourceValue) {
        bytes = resourceValue.value;
    }

    @Override
    public void visit(DateResourceValue resourceValue) {
        bytes = String.valueOf(resourceValue.value.getTime()).getBytes(Charset.forName("US-ASCII"));
    }

    @Override
    public void visit(DoubleResourceValue resourceValue) {
        setAsciiBytes(resourceValue);
    }

    @Override
    public void visit(FloatResourceValue resourceValue) {
        setAsciiBytes(resourceValue);
    }

    @Override
    public void visit(LongResourceValue resourceValue) {
        setAsciiBytes(resourceValue);
    }

    private void setAsciiBytes(ResourceValue resourceValue) {
        bytes = resourceValue.value.toString().getBytes(Charset.forName("US-ASCII"));
    }
}
