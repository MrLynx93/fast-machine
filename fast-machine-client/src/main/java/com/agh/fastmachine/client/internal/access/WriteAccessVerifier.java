package com.agh.fastmachine.client.internal.access;

import com.agh.fastmachine.client.api.model.AbstractLwm2mNode;
import com.agh.fastmachine.client.api.model.ObjectInstance;
import com.agh.fastmachine.client.api.model.ObjectResource;
import com.agh.fastmachine.client.internal.exception.ResourcesAccessRightsException;
import com.agh.fastmachine.client.internal.visitor.access.WriteAccessVisitor;

/**
 * - For {@link ObjectResource} checks if this resource has WRITE access granted
 * - For {@link ObjectInstance} checks if every of resources has WRITE access granted
 */
public class WriteAccessVerifier {

    /**
     * @param node       - node to which LWM2M client want to write. It is used in this method because it has access rights inside
     * @param parsedNode - node that was sent in LWM2M write request (only it's resources are checked)
     */
    public static void checkWriteAccess(AbstractLwm2mNode node, AbstractLwm2mNode parsedNode) throws ResourcesAccessRightsException {
        WriteAccessVisitor writeAccessVisitor = new WriteAccessVisitor(node);
        parsedNode.accept(writeAccessVisitor);
        if (writeAccessVisitor.nonWritableResourceExists()) {
            throw new ResourcesAccessRightsException();
        }
    }

}
