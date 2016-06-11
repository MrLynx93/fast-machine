package com.agh.fastmachine.client.internal.visitor;


import com.agh.fastmachine.client.api.model.ObjectBase;
import com.agh.fastmachine.client.api.model.ObjectInstance;
import com.agh.fastmachine.client.api.model.ObjectMultipleResource;
import com.agh.fastmachine.client.api.model.ObjectResource;
import com.agh.fastmachine.core.api.model.resourcevalue.ResourceValue;

public interface ObjectNodeVisitor {

    <T extends ObjectInstance> void visit(ObjectBase<T> objectBase);

    void visit(ObjectInstance objectInstance);

    <T extends ResourceValue<?>> void visit(ObjectResource<T> objectResource);

    <T extends ResourceValue<?>> void visit(ObjectMultipleResource<T> objectMultipleResource);

}
