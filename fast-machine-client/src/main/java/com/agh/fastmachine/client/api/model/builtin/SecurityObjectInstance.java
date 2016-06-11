package com.agh.fastmachine.client.api.model.builtin;

import com.agh.fastmachine.client.api.model.ObjectInstance;
import com.agh.fastmachine.client.api.model.ObjectResource;
import com.agh.fastmachine.core.api.model.annotation.Lwm2mResource;
import com.agh.fastmachine.core.api.model.annotation.Mandatory;
import com.agh.fastmachine.core.api.model.resourcevalue.BooleanResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.IntegerResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.StringResourceValue;

public class SecurityObjectInstance extends ObjectInstance {

    @Lwm2mResource(id = 10)
    public ObjectResource<IntegerResourceValue> shortServerId = new ObjectResource<>();

    @Mandatory
    @Lwm2mResource(id = 0)
    public ObjectResource<StringResourceValue> serverUri = new ObjectResource<>();

    @Mandatory
    @Lwm2mResource(id = 1)
    public ObjectResource<BooleanResourceValue> bootstrapServer = new ObjectResource<>();

    @Lwm2mResource(id = 11)
    public ObjectResource<IntegerResourceValue> clientHoldOffTime = new ObjectResource<>();

    public SecurityObjectInstance(int id) {
        super(id);
    }
}
