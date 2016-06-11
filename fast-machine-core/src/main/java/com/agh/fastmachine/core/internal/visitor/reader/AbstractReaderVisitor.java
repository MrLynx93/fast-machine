package com.agh.fastmachine.core.internal.visitor.reader;

import com.agh.fastmachine.core.internal.model.ObjectNodeModel;
import com.agh.fastmachine.core.internal.visitor.NodeVisitor;

public abstract class AbstractReaderVisitor implements NodeVisitor {
    public abstract <T extends ObjectNodeModel> T read(T node, byte[] content);
}
