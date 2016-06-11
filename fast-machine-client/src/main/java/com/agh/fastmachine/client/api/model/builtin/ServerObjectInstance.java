package com.agh.fastmachine.client.api.model.builtin;

import com.agh.fastmachine.client.api.model.ObjectInstance;
import com.agh.fastmachine.client.api.model.ObjectResource;
import com.agh.fastmachine.core.api.model.annotation.Lwm2mResource;
import com.agh.fastmachine.core.api.model.annotation.Mandatory;
import com.agh.fastmachine.core.api.model.resourcevalue.BooleanResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.IntegerResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.StringResourceValue;

import static com.agh.fastmachine.core.api.model.Operations.*;

public class ServerObjectInstance extends ObjectInstance {

    @Mandatory
    @Lwm2mResource(id = 0, permissions = READ)
    public ObjectResource<IntegerResourceValue> shortServerId = new ObjectResource<>();

    @Mandatory
    @Lwm2mResource(id = 1, permissions = READ | WRITE)
    public ObjectResource<IntegerResourceValue> lifetime = new ObjectResource<>();

    @Mandatory
    @Lwm2mResource(id = 7, permissions = READ | WRITE)
    public ObjectResource<StringResourceValue> binding = new ObjectResource<>();

    @Lwm2mResource(id = 2, permissions = READ | WRITE)
    public ObjectResource<IntegerResourceValue> defaultMinimumPeriod = new ObjectResource<>();

    @Lwm2mResource(id = 3, permissions = READ | WRITE)
    public ObjectResource<IntegerResourceValue> defaultMaximumPeriod = new ObjectResource<>();

    @Mandatory
    @Lwm2mResource(id = 4, permissions = EXECUTE)
    public ObjectResource<BooleanResourceValue> disable = new ObjectResource<>();

    @Lwm2mResource(id = 5, permissions = READ | WRITE)
    public ObjectResource<IntegerResourceValue> disableTimeout = new ObjectResource<>();

    @Lwm2mResource(id = 6, permissions = READ | WRITE)
    public ObjectResource<BooleanResourceValue> storeNotifications = new ObjectResource<>();

    @Lwm2mResource(id = 8, permissions = EXECUTE)// TODO special resource execute only?
    public ObjectResource<BooleanResourceValue> triggerUpdate = new ObjectResource<>();

    public ServerObjectInstance(int id) {
        super(id);
    }

    public Integer getShortServerId() {
        return shortServerId.getValue().value;
    }
}
