package com.agh.fastmachine.core.internal.parser;

import com.agh.fastmachine.core.internal.model.ObjectNodeModel;
import com.agh.fastmachine.core.api.model.resourcevalue.ResourceValue;
import com.agh.fastmachine.core.internal.parser.resolver.ReadVisitorResolver;
import com.agh.fastmachine.core.internal.visitor.reader.AbstractReaderVisitor;
import com.agh.fastmachine.core.internal.visitor.reader.PlainTextReaderVisitor;

/**
 * Parses bytes[] to {@link ObjectNodeModel} or {@link ResourceValue}
 * - for {@link ObjectNodeModel} takes node to determine return type
 * - for {@link ResourceValue} takes resourceValue to determine return type
 */
public abstract class ReadParser {
    protected ObjectFactory objectFactory;

    public <T extends ObjectNodeModel> T deserialize(T node, byte[] content) {
        AbstractReaderVisitor parser = new ReadVisitorResolver(objectFactory).getReader(node);
        return parser.read(node, content);
    }

    public <T extends ResourceValue<?>> T deserialize(T currentValue, byte[] content) {
        PlainTextReaderVisitor parser = new PlainTextReaderVisitor(objectFactory);
        return (T) parser.read(currentValue, content);
    }
}
