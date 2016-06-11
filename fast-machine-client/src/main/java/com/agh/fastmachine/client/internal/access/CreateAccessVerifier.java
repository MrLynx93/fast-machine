package com.agh.fastmachine.client.internal.access;

import com.agh.fastmachine.client.api.model.ObjectInstance;
import com.agh.fastmachine.client.api.model.ObjectResource;
import com.agh.fastmachine.core.internal.model.ObjectInstanceModel;
import com.agh.fastmachine.core.api.model.Operations;

public class CreateAccessVerifier {

    public static boolean verify(ObjectInstance instance, ObjectInstanceModel data) {
        for (ObjectResource<?> objectResource : instance.getObjectResources().values()) {
            if (data.getResources().get(objectResource.getId()) == null)
                if (objectResource.isMandatory()) // todo: why?
                    return false;
                else if ((objectResource.getAllowedOperations() & Operations.WRITE) == 0)
                    return false;
        }
        return true;
    }

}
