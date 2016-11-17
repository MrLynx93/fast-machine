package com.agh.fastmachine.core.internal.visitor.writer;

import com.agh.fastmachine.core.api.model.resourcevalue.*;
import com.agh.fastmachine.core.internal.model.*;
import com.agh.fastmachine.core.internal.visitor.NodeVisitor;

public abstract class AbstractWriterVisitor implements NodeVisitor {

    protected abstract byte[] getBytes();

    public byte[] parseNodeModel(ObjectNodeModel objectInstance) {
        objectInstance.accept(this);
        return getBytes();
    }

    @Override
    public <T extends ObjectInstanceModel> void visit(ObjectBaseModel<T> objectBase) {

    }

    @Override
    public void visit(ObjectInstanceModel objectInstance) {

    }

    @Override
    public void visit(ObjectResourceModel objectResource) {
        throw new IllegalStateException();
    }

    @Override
    public void visit(ObjectMultipleResourceModel objectMultipleResource) {
        throw new IllegalStateException();
    }

    @Override
    public void visit(StringResourceValue resourceValue) {
        throw new IllegalStateException();
    }

    @Override
    public void visit(IntegerResourceValue resourceValue) {
        throw new IllegalStateException();
    }

    @Override
    public void visit(BooleanResourceValue resourceValue) {
        throw new IllegalStateException();
    }

    @Override
    public void visit(DateResourceValue resourceValue) {
        throw new IllegalStateException();
    }

    @Override
    public void visit(DoubleResourceValue resourceValue) {
        throw new IllegalStateException();
    }

    @Override
    public void visit(FloatResourceValue resourceValue) {
        throw new IllegalStateException();
    }

    @Override
    public void visit(LongResourceValue resourceValue) {
        throw new IllegalStateException();
    }

    @Override
    public void visit(OpaqueResourceValue resourceValue) {
        throw new IllegalStateException();
    }

    @Override
    public void visit(LinkResourceValue resourceValue) {
        throw new IllegalStateException();
    }
}
