package com.agh.fastmachine.core.internal.visitor;

public interface VisitableNode {
    void accept(NodeVisitor visitor);
}
