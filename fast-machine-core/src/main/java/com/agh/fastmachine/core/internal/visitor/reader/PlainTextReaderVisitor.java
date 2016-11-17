package com.agh.fastmachine.core.internal.visitor.reader;

import com.agh.fastmachine.core.internal.model.*;
import com.agh.fastmachine.core.internal.parser.ObjectFactory;
import com.agh.fastmachine.core.api.model.resourcevalue.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.util.Date;

public class PlainTextReaderVisitor extends AbstractReaderVisitor {

    private final Logger LOG = LoggerFactory.getLogger(PlainTextReaderVisitor.class);
    private final ObjectFactory objectFactory;
    private ResourceValue<?> resourceValue;
    private ObjectResourceModel<?> resourceModel;

    private byte[] content;

    public PlainTextReaderVisitor(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    @Override
    public <T extends ObjectNodeModel> T read(T object, byte[] content) {
        this.content = content;
        object.accept(this);
        return (T) resourceModel;
    }

    public ResourceValue<?> read(ResourceValue<?> resourceValue, byte[] content) {
        this.content = content;
        resourceValue.accept(this);
        return this.resourceValue;
    }

    @Override
    public <T extends ResourceValue<?>> void visit(ObjectResourceModel<T> objectResource) {
        if (objectResource.getValue() == null) {
            try {
                Constructor twoArgConstructor = null;
                for (Constructor constructor : objectResource.getValueType().getConstructors())
                    if (constructor.getParameterTypes().length == 2)
                        twoArgConstructor = constructor;
                ((ResourceValue) twoArgConstructor.newInstance(null, 0)).accept(this);
            } catch (Exception e) {
                LOG.error("Two arguments constructor of ResourceValue not found");
            }
        }
        else
            objectResource.getValue().accept(this);
        resourceModel = objectFactory.createObjectResource(objectResource.getId(), (T) resourceValue, objectResource.getValueType());
    }

    @Override
    public void visit(StringResourceValue resourceValue) {
        this.resourceValue = new StringResourceValue(new String(content, Charset.forName("UTF-8")));
    }

    @Override
    public void visit(IntegerResourceValue resourceValue) {
        this.resourceValue = new IntegerResourceValue(new Integer(new String(content, Charset.forName("US-ASCII"))));
    }

    @Override
    public void visit(BooleanResourceValue resourceValue) {
        this.resourceValue = new BooleanResourceValue(content[0] == '1');
    }

    @Override
    public void visit(DateResourceValue resourceValue) {
        this.resourceValue = new DateResourceValue(new Date(new Long(new String(content, Charset.forName("US-ASCII")))));
    }

    @Override
    public void visit(DoubleResourceValue resourceValue) {
        this.resourceValue = new DoubleResourceValue(new Double(new String(content, Charset.forName("US-ASCII"))));
    }

    @Override
    public void visit(FloatResourceValue resourceValue) {
        this.resourceValue = new FloatResourceValue(new Float(new String(content, Charset.forName("US-ASCII"))));
    }

    @Override
    public void visit(LongResourceValue resourceValue) {
        this.resourceValue = new LongResourceValue(new Long(new String(content, Charset.forName("US-ASCII"))));
    }

    @Override
    public void visit(OpaqueResourceValue resourceValue) {
        this.resourceValue = new OpaqueResourceValue(content); //TODO to trzeba by usunąć, bo to nie plaintext
    }

    @Override
    public void visit(LinkResourceValue resourceValue) {
        String[] elements = new String(content, Charset.forName("US-ASCII")).split(":");
        Link link = new Link(Integer.parseInt(elements[0]), Integer.parseInt(elements[1]));
        this.resourceValue = new LinkResourceValue(link);
    }

    //////////////////////////NOT USED/////////////////////////////////

    @Override
    public <T extends ObjectInstanceModel> void visit(ObjectBaseModel<T> objectBase) {

    }

    @Override
    public void visit(ObjectInstanceModel objectInstance) {

    }

    @Override
    public <T extends ResourceValue<?>> void visit(ObjectMultipleResourceModel<T> objectMultipleResource) {

    }


}
