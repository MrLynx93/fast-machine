package com.agh.fastmachine.server.api.model.builtin;

import com.agh.fastmachine.core.api.model.annotation.Lwm2mObjectInstance;
import com.agh.fastmachine.core.api.model.annotation.Lwm2mResource;
import com.agh.fastmachine.core.api.model.resourcevalue.BooleanResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.IntegerResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.StringResourceValue;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;

@Lwm2mObjectInstance(objectId = 1)
public class ServerObjectInstanceProxy extends ObjectInstanceProxy {

    @Lwm2mResource(id = 0)
    public ObjectResourceProxy<IntegerResourceValue> shortServerId;

    @Lwm2mResource(id = 1)
    public ObjectResourceProxy<IntegerResourceValue> lifetime;

    @Lwm2mResource(id = 2)
    public ObjectResourceProxy<IntegerResourceValue> defaultMinimumPeriod;

    @Lwm2mResource(id = 3)
    public ObjectResourceProxy<IntegerResourceValue> defaultMaximumPeriod;

    @Lwm2mResource(id = 4)
    public ObjectResourceProxy<BooleanResourceValue> disable;

    @Lwm2mResource(id = 5)
    public ObjectResourceProxy<IntegerResourceValue> disableTimeout;

    @Lwm2mResource(id = 6)
    public ObjectResourceProxy<BooleanResourceValue> storeNotifications;

    @Lwm2mResource(id = 7)
    public ObjectResourceProxy<StringResourceValue> binding;

    @Lwm2mResource(id = 8)
    public ObjectResourceProxy<BooleanResourceValue> triggerUpdate;

    public ServerObjectInstanceProxy() {
    }

    public ServerObjectInstanceProxy(int id) {
        super(id);
    }
}
