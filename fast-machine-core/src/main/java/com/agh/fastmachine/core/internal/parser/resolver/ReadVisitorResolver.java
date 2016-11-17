package com.agh.fastmachine.core.internal.parser.resolver;

import com.agh.fastmachine.core.api.model.resourcevalue.*;
import com.agh.fastmachine.core.internal.model.*;
import com.agh.fastmachine.core.internal.parser.ObjectFactory;
import com.agh.fastmachine.core.internal.visitor.NodeVisitor;
import com.agh.fastmachine.core.internal.visitor.reader.AbstractReaderVisitor;
import com.agh.fastmachine.core.internal.visitor.reader.PlainTextReaderVisitor;
import com.agh.fastmachine.core.internal.visitor.reader.TLVReaderVisitor;

public class ReadVisitorResolver implements NodeVisitor {
    private AbstractReaderVisitor reader;
    private ObjectFactory objectFactory;

    public ReadVisitorResolver(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    public AbstractReaderVisitor getReader(ObjectNodeModel node) {
        node.accept(this);
        return reader;
    }

    @Override
    public <T extends ObjectInstanceModel> void visit(ObjectBaseModel<T> objectBase) {
        reader = new TLVReaderVisitor(objectFactory);
    }

    @Override
    public void visit(ObjectInstanceModel objectInstance) {
        reader = new TLVReaderVisitor(objectFactory);
    }

    @Override
    public <T extends ResourceValue<?>> void visit(ObjectResourceModel<T> objectResource) {
        reader = new PlainTextReaderVisitor(objectFactory);
    }

    @Override
    public <T extends ResourceValue<?>> void visit(ObjectMultipleResourceModel<T> objectMultipleResource) {
        reader = new TLVReaderVisitor(objectFactory);
    }

    @Override
    public void visit(StringResourceValue resourceValue) {
        reader = new PlainTextReaderVisitor(objectFactory);
    }

    @Override
    public void visit(IntegerResourceValue resourceValue) {
        reader = new PlainTextReaderVisitor(objectFactory);
    }

    @Override
    public void visit(BooleanResourceValue resourceValue) {
        reader = new PlainTextReaderVisitor(objectFactory);
    }

    @Override
    public void visit(DateResourceValue resourceValue) {
        reader = new PlainTextReaderVisitor(objectFactory);
    }

    @Override
    public void visit(DoubleResourceValue resourceValue) {
        reader = new PlainTextReaderVisitor(objectFactory);
    }

    @Override
    public void visit(FloatResourceValue resourceValue) {
        reader = new PlainTextReaderVisitor(objectFactory);
    }

    @Override
    public void visit(LongResourceValue resourceValue) {
        reader = new PlainTextReaderVisitor(objectFactory);
    }

    @Override
    public void visit(OpaqueResourceValue resourceValue) {
        reader = new PlainTextReaderVisitor(objectFactory);
    }

    @Override
    public void visit(LinkResourceValue resourceValue) {
        reader = new PlainTextReaderVisitor(objectFactory);
    }
}
