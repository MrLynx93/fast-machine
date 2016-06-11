package com.agh.fastmachine.client.internal.visitor.merge;

import com.agh.fastmachine.client.api.model.ObjectBase;
import com.agh.fastmachine.client.api.model.ObjectInstance;
import com.agh.fastmachine.client.api.model.ObjectMultipleResource;
import com.agh.fastmachine.client.api.model.ObjectResource;
import com.agh.fastmachine.client.internal.visitor.ObjectNodeVisitor;
import com.agh.fastmachine.core.internal.model.ObjectInstanceModel;
import com.agh.fastmachine.core.internal.model.ObjectMultipleResourceModel;
import com.agh.fastmachine.core.internal.model.ObjectNodeModel;
import com.agh.fastmachine.core.internal.model.ObjectResourceModel;
import com.agh.fastmachine.core.api.model.resourcevalue.ResourceValue;

class MergeVisitor implements ObjectNodeVisitor {
    private ObjectNodeModel newModel;

    public MergeVisitor(ObjectNodeModel newModel) {
        this.newModel = newModel;
    }

    @Override
    public <T extends ObjectInstance> void visit(ObjectBase<T> objectBase) {

    }

    @Override
    public void visit(ObjectInstance objectInstance) {
        ObjectInstanceModel newInstance = (ObjectInstanceModel) newModel;
        for (ObjectResourceModel objectResourceModel : newInstance.getResources().values()) {
            newModel = objectResourceModel;
            objectInstance.getResource(objectResourceModel.getId()).accept(this);
        }

    }

    @Override
    public <T extends ResourceValue<?>> void visit(ObjectResource<T> objectResource) {
        objectResource.setValue(((ObjectResourceModel<T>) newModel).getValue());
    }

    @Override
    public <T extends ResourceValue<?>> void visit(ObjectMultipleResource<T> objectMultipleResource) {
        for (T newValue : ((ObjectMultipleResourceModel<T>) newModel).getValues().values())
            objectMultipleResource.setValue(newValue);
    }
}