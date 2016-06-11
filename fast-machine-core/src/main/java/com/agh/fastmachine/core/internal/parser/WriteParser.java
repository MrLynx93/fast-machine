package com.agh.fastmachine.core.internal.parser;

import com.agh.fastmachine.core.internal.parser.resolver.WriteVisitorResolver;
import com.agh.fastmachine.core.internal.model.ObjectNodeModel;
import com.agh.fastmachine.core.internal.visitor.writer.AbstractWriterVisitor;

/**
 * Parses {@link ObjectNodeModel} to byte[]. Don't have to take
 * a node in argument as in s{@link ReadParser} because parsing
 * removes explicit information about type, object id etc.
 */
public class WriteParser {

    public static byte[] serialize(ObjectNodeModel node) {
        AbstractWriterVisitor parser = new WriteVisitorResolver().getWriter(node);
        return parser.parseNodeModel(node);
    }

}
