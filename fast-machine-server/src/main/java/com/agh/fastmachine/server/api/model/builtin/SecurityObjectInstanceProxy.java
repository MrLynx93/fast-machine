package com.agh.fastmachine.server.api.model.builtin;

import com.agh.fastmachine.core.api.model.annotation.Lwm2mObjectInstance;
import com.agh.fastmachine.core.api.model.annotation.Lwm2mResource;
import com.agh.fastmachine.core.api.model.resourcevalue.BooleanResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.IntegerResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.StringResourceValue;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;

@Lwm2mObjectInstance(objectId = 0)
public class SecurityObjectInstanceProxy extends ObjectInstanceProxy {

    @Lwm2mResource(id = 10)
    public ObjectResourceProxy<IntegerResourceValue> shortServerId;

    @Lwm2mResource(id = 0)
    public ObjectResourceProxy<StringResourceValue> serverUri;

    @Lwm2mResource(id = 1)
    public ObjectResourceProxy<BooleanResourceValue> bootstrapServer;

    @Lwm2mResource(id = 11)
    public ObjectResourceProxy<IntegerResourceValue> clientHoldOffTime;


    public SecurityObjectInstanceProxy() {
    }

    public SecurityObjectInstanceProxy(int id) {
        super(id);
    }
}
