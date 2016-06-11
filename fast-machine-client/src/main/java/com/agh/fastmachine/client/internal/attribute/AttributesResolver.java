package com.agh.fastmachine.client.internal.attribute;

import com.agh.fastmachine.client.api.model.AbstractLwm2mNode;
import com.agh.fastmachine.client.api.model.ObjectBase;
import com.agh.fastmachine.client.api.model.ObjectInstance;
import com.agh.fastmachine.client.api.model.ObjectResource;
import com.agh.fastmachine.client.internal.visitor.AttributesVisitor;
import com.agh.fastmachine.core.api.model.Attributes;

import java.util.HashMap;
import java.util.Map;

/**
 * Returns merged {@link Attributes} for any LWM2M node
 * - for {@link ObjectBase} returns its {@link Attributes}
 *
 * - for {@link ObjectInstance} returns its attributes and fills missing ones with its
 * parent ({@link ObjectBase})'s attributes
 *
 * - for {@link ObjectResource} returns its attributes and fills missing ones with its
 * parent ({@link ObjectInstance})'s attributes and then fills missing with ones with its
 * parent ({@link ObjectBase})'s attributes
 */
public class AttributesResolver {

    public static Attributes getMergedAttributes(AbstractLwm2mNode node, int shortServerId) {
        Attributes attributes = node.getWriteAttributes(shortServerId);
        AttributesVisitor attributesVisitor = new AttributesVisitor(attributes, shortServerId);
        node.accept(attributesVisitor);
        return attributesVisitor.getMergedAttributes();
    }

    public static Map<Integer, Attributes> getMergedAttributes(AbstractLwm2mNode node) {
        HashMap<Integer, Attributes> attributes = new HashMap<>();
        for (Integer shortServerId : node.getWriteAttributes().keySet()) {
            attributes.put(shortServerId, getMergedAttributes(node, shortServerId));
        }
        return attributes;
    }
}
