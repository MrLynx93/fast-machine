package com.test.util.model;

import com.agh.fastmachine.core.api.model.annotation.Lwm2mObjectInstance;
import com.agh.fastmachine.core.api.model.annotation.Lwm2mResource;
import com.agh.fastmachine.core.api.model.resourcevalue.BooleanResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.FloatResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.IntegerResourceValue;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;

@Lwm2mObjectInstance(objectId = 20001)
public class PingInstanceProxy extends ObjectInstanceProxy {

    @Lwm2mResource(id = 0)
    public ObjectResourceProxy<BooleanResourceValue> executePing;

    @Lwm2mResource(id = 1)
    public ObjectResourceProxy<IntegerResourceValue> lastExecuteTime;

    @Lwm2mResource(id = 2)
    public ObjectResourceProxy<IntegerResourceValue> errorCode;

    @Lwm2mResource(id = 3)
    public ObjectResourceProxy<IntegerResourceValue> packetsTransmitted;

    @Lwm2mResource(id = 4)
    public ObjectResourceProxy<IntegerResourceValue> packetsReceived;

    @Lwm2mResource(id = 5)
    public ObjectResourceProxy<FloatResourceValue> rtt;

    @Lwm2mResource(id = 6)
    public ObjectResourceProxy<FloatResourceValue> averageTime;

    @Lwm2mResource(id = 7)
    public ObjectResourceProxy<FloatResourceValue> successRate;
}
