package com.agh.fastmachine.client.internal.access;

import com.agh.fastmachine.client.api.model.AbstractLwm2mNode;
import com.agh.fastmachine.client.api.model.ObjectBase;
import com.agh.fastmachine.client.api.model.ObjectInstance;
import com.agh.fastmachine.client.api.model.ObjectResource;
import com.agh.fastmachine.client.internal.exception.ResourcesAccessRightsException;
import com.agh.fastmachine.client.internal.visitor.access.ReadAccessFilterVisitor;

/**
 * Scans {@link ObjectBase}, {@link ObjectInstance} or {@link ObjectResource} and
 * removes those, for which READ access is not granted.
 * <p/>
 * If argument is a resource, then it returns:
 * - this {@link ObjectResource} if you have READ access
 * - null if you don't have READ access
 */
public class ReadAccessFilter {

    public static AbstractLwm2mNode filter(AbstractLwm2mNode node) throws ResourcesAccessRightsException {
        ReadAccessFilterVisitor filterVisitor = new ReadAccessFilterVisitor();
        node.accept(filterVisitor);
        AbstractLwm2mNode filteredNode = filterVisitor.getFilteredNode();
        if (filteredNode == null) {
            throw new ResourcesAccessRightsException();
        }
        return filteredNode;
    }

}
