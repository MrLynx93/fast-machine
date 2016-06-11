package com.agh.fastmachine.core.internal.visitor;

import com.agh.fastmachine.core.internal.model.ObjectBaseModel;
import com.agh.fastmachine.core.internal.model.ObjectInstanceModel;
import com.agh.fastmachine.core.internal.model.ObjectMultipleResourceModel;
import com.agh.fastmachine.core.internal.model.ObjectResourceModel;
import com.agh.fastmachine.core.api.model.resourcevalue.*;

public interface NodeVisitor {

    <T extends ObjectInstanceModel> void visit(ObjectBaseModel<T> objectBase);

    void visit(ObjectInstanceModel objectInstance);

    <T extends ResourceValue<?>> void visit(ObjectResourceModel<T> objectResource);

    <T extends ResourceValue<?>> void visit(ObjectMultipleResourceModel<T> objectMultipleResource);

    void visit(StringResourceValue resourceValue);

    void visit(IntegerResourceValue resourceValue);

    void visit(BooleanResourceValue resourceValue);

    void visit(DateResourceValue resourceValue);

    void visit(DoubleResourceValue resourceValue);

    void visit(FloatResourceValue resourceValue);

    void visit(LongResourceValue resourceValue);

    void visit(OpaqueResourceValue resourceValue);
}
