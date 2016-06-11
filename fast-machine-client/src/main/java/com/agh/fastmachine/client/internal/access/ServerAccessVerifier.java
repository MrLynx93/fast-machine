package com.agh.fastmachine.client.internal.access;

import com.agh.fastmachine.client.api.model.AbstractLwm2mNode;
import com.agh.fastmachine.client.internal.exception.ServerAccessRightsException;
import com.agh.fastmachine.client.internal.visitor.access.AccessRightsVisitor;

public class ServerAccessVerifier {

    public static AbstractLwm2mNode checkAccessRights(AbstractLwm2mNode node, int shortServerId, int operationToCheck) throws ServerAccessRightsException {
        AccessRightsVisitor accessRightsVisitor = new AccessRightsVisitor(shortServerId, operationToCheck, node);
        AbstractLwm2mNode resultNode = accessRightsVisitor.getResultNode();
        if (resultNode == null) {
            throw new ServerAccessRightsException();
        }
        return resultNode;
    }
}
