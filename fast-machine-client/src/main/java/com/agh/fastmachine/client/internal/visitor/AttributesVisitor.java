package com.agh.fastmachine.client.internal.visitor;

import com.agh.fastmachine.client.api.model.*;
import com.agh.fastmachine.core.api.model.Attributes;
import com.agh.fastmachine.core.api.model.resourcevalue.ResourceValue;

public class AttributesVisitor implements ObjectNodeVisitor {
    private Attributes attributes;
    private int shortServerId;

    public AttributesVisitor(Attributes attributes, int shortServerId) {
        this.attributes = attributes;
        this.shortServerId = shortServerId;
    }

    public Attributes getMergedAttributes() {
        return attributes;
    }

    @Override
    public <T extends ObjectInstance> void visit(ObjectBase<T> objectBase) {
        fillMissingAttributes(objectBase.getWriteAttributes(shortServerId));
    }

    @Override
    public void visit(ObjectInstance instance) {
        fillMissingAttributes(instance.getWriteAttributes(shortServerId));
        instance.parent().accept(this);
    }

    @Override
    public <T extends ResourceValue<?>> void visit(ObjectResource<T> resource) {
        fillMissingAttributes(resource.getWriteAttributes(shortServerId));
        resource.getInstance().accept(this);
    }

    @Override
    public <T extends ResourceValue<?>> void visit(ObjectMultipleResource<T> resource) {
        fillMissingAttributes(resource.getWriteAttributes(shortServerId));
        this.attributes.dimension = resource.getWriteAttributes(shortServerId).dimension;
        resource.getInstance().accept(this);
    }

    private void fillMissingAttributes(Attributes attributes) {
        if(this.attributes.minimumPeriod == null) { // TODO znowu nullpointer
            this.attributes.minimumPeriod = attributes.minimumPeriod;
        }
        if(this.attributes.maximumPeriod == null) {
            this.attributes.maximumPeriod = attributes.maximumPeriod;
        }
        if(this.attributes.greaterThan == null) {
            this.attributes.greaterThan = attributes.greaterThan;
        }
        if(this.attributes.lessThan == null) {
            this.attributes.lessThan = attributes.lessThan;
        }
        if(this.attributes.step == null) {
            this.attributes.step = attributes.step;
        }
    }
}
