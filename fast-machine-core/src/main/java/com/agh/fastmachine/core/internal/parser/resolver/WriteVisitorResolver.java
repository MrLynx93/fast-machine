package com.agh.fastmachine.core.internal.parser.resolver;

import com.agh.fastmachine.core.api.model.resourcevalue.*;
import com.agh.fastmachine.core.internal.model.*;
import com.agh.fastmachine.core.internal.visitor.NodeVisitor;
import com.agh.fastmachine.core.internal.visitor.writer.AbstractWriterVisitor;
import com.agh.fastmachine.core.internal.visitor.writer.PlainTextWriterVisitor;
import com.agh.fastmachine.core.internal.visitor.writer.TLVWriterVisitor;

public class WriteVisitorResolver implements NodeVisitor {
    private AbstractWriterVisitor writer;

    public AbstractWriterVisitor getWriter(ObjectNodeModel node) {
        node.accept(this);
        return writer;
    }

    @Override
    public <T extends ObjectInstanceModel> void visit(ObjectBaseModel<T> objectBase) {
        writer = new TLVWriterVisitor();
    }

    @Override
    public void visit(ObjectInstanceModel objectInstance) {
        writer = new TLVWriterVisitor();
    }

    @Override
    public <T extends ResourceValue<?>> void visit(ObjectResourceModel<T> objectResource) {
        writer = new PlainTextWriterVisitor();
    }

    @Override
    public <T extends ResourceValue<?>> void visit(ObjectMultipleResourceModel<T> objectMultipleResource) {
        writer = new TLVWriterVisitor();
    }

    @Override
    public void visit(StringResourceValue resourceValue) {
        writer = new PlainTextWriterVisitor();
    }

    @Override
    public void visit(IntegerResourceValue resourceValue) {
        writer = new PlainTextWriterVisitor();
    }

    @Override
    public void visit(BooleanResourceValue resourceValue) {
        writer = new PlainTextWriterVisitor();
    }

    @Override
    public void visit(DateResourceValue resourceValue) {
        writer = new PlainTextWriterVisitor();
    }

    @Override
    public void visit(DoubleResourceValue resourceValue) {
        writer = new PlainTextWriterVisitor();
    }

    @Override
    public void visit(FloatResourceValue resourceValue) {
        writer = new PlainTextWriterVisitor();
    }

    @Override
    public void visit(LongResourceValue resourceValue) {
        writer = new PlainTextWriterVisitor();
    }

    @Override
    public void visit(OpaqueResourceValue resourceValue) {
        writer = new PlainTextWriterVisitor();
    }

    @Override
    public void visit(LinkResourceValue resourceValue) {
        writer = new PlainTextWriterVisitor();
    }
}
