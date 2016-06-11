package com.agh.fastmachine.client.internal.visitor;

public interface VisitableObjectNode {
    void accept(ObjectNodeVisitor visitor);
}
