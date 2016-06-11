package com.agh.fastmachine.client.internal.visitor.access;

import com.agh.fastmachine.client.api.model.AbstractLwm2mNode;
import com.agh.fastmachine.client.api.model.ObjectInstance;
import com.agh.fastmachine.client.api.model.ObjectResource;
import com.agh.fastmachine.core.internal.model.ObjectBaseModel;
import com.agh.fastmachine.core.internal.model.ObjectInstanceModel;
import com.agh.fastmachine.core.internal.model.ObjectMultipleResourceModel;
import com.agh.fastmachine.core.internal.model.ObjectResourceModel;
import com.agh.fastmachine.core.api.model.resourcevalue.*;
import com.agh.fastmachine.core.api.model.Operations;
import com.agh.fastmachine.core.internal.visitor.NodeVisitor;


public class WriteAccessVisitor implements NodeVisitor {
    private AbstractLwm2mNode current;
    private boolean isWritable = true;

    public WriteAccessVisitor(AbstractLwm2mNode current) {
        this.current = current;
    }

    public boolean nonWritableResourceExists() {
        return !isWritable;
    }

    @Override
    public void visit(ObjectInstanceModel newInstance) {
        ObjectInstance currentInstance = (ObjectInstance) current;

        for (ObjectResourceModel<?> newResource : newInstance.getResources().values()) {
            ObjectResource<?> currentResource = currentInstance.getObjectResources().get(newResource.getId());
            if ((currentResource.getAllowedOperations() & Operations.WRITE) == 0) {
                isWritable = false;
                break;
            }
        }
    }

    @Override
    public <T extends ResourceValue<?>> void visit(ObjectResourceModel<T> objectResourceModel) {
        visitResource(objectResourceModel);
    }

    private <T extends ResourceValue<?>> void visitResource(ObjectResourceModel<T> objectResourceModel) {
        ObjectResource<T> objectResource = (ObjectResource<T>) current;
        if ((objectResource.getAllowedOperations() & Operations.WRITE) == 0) {
            isWritable = false;
        }
    }

    @Override
    public <T extends ResourceValue<?>> void visit(ObjectMultipleResourceModel<T> objectMultipleResourceModel) {
        visitResource(objectMultipleResourceModel);
    }

    @Override
    public void visit(StringResourceValue resourceValue) {

    }

    @Override
    public void visit(IntegerResourceValue resourceValue) {

    }

    @Override
    public void visit(BooleanResourceValue resourceValue) {

    }

    @Override
    public void visit(DateResourceValue resourceValue) {

    }

    @Override
    public void visit(DoubleResourceValue resourceValue) {

    }

    @Override
    public void visit(FloatResourceValue resourceValue) {

    }

    @Override
    public void visit(LongResourceValue resourceValue) {

    }

    @Override
    public void visit(OpaqueResourceValue resourceValue) {

    }

    @Override
    public <T extends ObjectInstanceModel> void visit(ObjectBaseModel<T> objectBase) {

    }

}
