package com.test.model;

import com.agh.fastmachine.core.api.model.annotation.Lwm2mObjectInstance;
import com.agh.fastmachine.core.api.model.annotation.Lwm2mResource;
import com.agh.fastmachine.core.api.model.resourcevalue.BooleanResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.IntegerResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.StringResourceValue;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;

@Lwm2mObjectInstance(objectId = 20002)
public class AndroidUtilsInstanceProxy extends ObjectInstanceProxy {

    @Lwm2mResource(id = 0)
    public ObjectResourceProxy<StringResourceValue> deviceId;

    @Lwm2mResource(id = 1)
    public ObjectResourceProxy<StringResourceValue> imei;

    @Lwm2mResource(id = 2)
    public ObjectResourceProxy<IntegerResourceValue> batteryLevel;

    @Lwm2mResource(id = 3)
    public ObjectResourceProxy<IntegerResourceValue> batteryState;

    @Lwm2mResource(id = 4)
    public ObjectResourceProxy<IntegerResourceValue> connectionType;

    @Lwm2mResource(id = 5)
    public ObjectResourceProxy<IntegerResourceValue> signalStrength;

    @Lwm2mResource(id = 6)
    public ObjectResourceProxy<BooleanResourceValue> bluetooth;

    @Lwm2mResource(id = 7)
    public ObjectResourceProxy<BooleanResourceValue> flashlight;

    @Lwm2mResource(id = 8)
    public ObjectResourceProxy<BooleanResourceValue> vibrate;
}

