package com.agh.fastmachine.client.internal.visitor.merge;

import com.agh.fastmachine.client.api.model.AbstractLwm2mNode;
import com.agh.fastmachine.client.api.model.ObjectInstance;
import com.agh.fastmachine.client.api.model.ObjectResource;
import com.agh.fastmachine.core.internal.model.ObjectNodeModel;

/**
 * - For {@link ObjectInstance} set resource values of sourceNode's resources into
 * destinationNode's resources based on resourceIds contained in {@link ObjectResource}
 * - For {@link ObjectResource} set value of sourceNode into destinationNode
 */
public class Lwm2mNodeMerger {

    public static void merge(AbstractLwm2mNode destinationNode, ObjectNodeModel sourceNode) { // TODO types are wrong ???
        MergeVisitor mergeVisitor = new MergeVisitor(sourceNode);
        destinationNode.accept(mergeVisitor);
    }

}
