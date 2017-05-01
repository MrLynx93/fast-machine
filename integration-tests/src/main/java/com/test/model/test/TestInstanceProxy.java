package com.test.model.test;

import com.agh.fastmachine.core.api.model.annotation.Lwm2mObjectInstance;
import com.agh.fastmachine.core.api.model.annotation.Lwm2mResource;
import com.agh.fastmachine.core.api.model.annotation.Mandatory;
import com.agh.fastmachine.core.api.model.resourcevalue.StringResourceValue;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;

import static com.agh.fastmachine.core.api.model.Operations.READ;
import static com.agh.fastmachine.core.api.model.Operations.WRITE;

@Lwm2mObjectInstance(objectId = 20004)
public class TestInstanceProxy extends ObjectInstanceProxy {

    @Mandatory
    @Lwm2mResource(id = 0, permissions = READ)
    public ObjectResourceProxy<StringResourceValue> clientId;

    @Mandatory
    @Lwm2mResource(id = 1, permissions = READ)
    public ObjectResourceProxy<StringResourceValue> serverId;

    @Lwm2mResource(id = 2, permissions = READ | WRITE)
    public ObjectResourceProxy<StringResourceValue> payload;
}
