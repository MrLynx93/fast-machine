package com.agh.fastmachine.server.api.model.builtin;

import com.agh.fastmachine.core.api.model.annotation.Lwm2mObjectInstance;
import com.agh.fastmachine.core.api.model.annotation.Lwm2mResource;
import com.agh.fastmachine.core.api.model.resourcevalue.IntegerResourceValue;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectMultipleResourceProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;

@Lwm2mObjectInstance(objectId = 2)
public class AccessControlObjectInstanceProxy extends ObjectInstanceProxy {

    @Lwm2mResource(id = 0)
    public ObjectResourceProxy<IntegerResourceValue> objectId;

    @Lwm2mResource(id = 1)
    public ObjectResourceProxy<IntegerResourceValue> instanceId;

    @Lwm2mResource(id = 2)
    public ObjectMultipleResourceProxy<IntegerResourceValue> accessControlList;

    @Lwm2mResource(id = 3)
    public ObjectResourceProxy<IntegerResourceValue> accessControlOwner;

}
