package com.test.util.model;

import com.agh.fastmachine.core.api.model.annotation.Lwm2mObjectInstance;
import com.agh.fastmachine.core.api.model.annotation.Lwm2mResource;
import com.agh.fastmachine.core.api.model.resourcevalue.*;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectMultipleResourceProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;

/**
 * * Example object:
 * +------------+------------------------------+
 * | Name       |   Example                    |
 * | ID         |   123                        |
 * | Instances  |   multiple                   |
 * | Mandatory  |   false                      |
 * | Object     |   URN urn:oma:lwm2m:oma:123  |
 * +------------+------------------------------+
 * <p>
 * Resources:
 * +----+-------------------------------------------+------------+-----------+-----------+---------+
 * |ID  | Name                                      | Operations | Instances | Mandatory | Type    |
 * +----+-------------------------------------------+------------+-----------+-----------+---------+
 * |0   | Battery level                             | RW         | Single    | Mandatory | Integer |
 * |1   | Double example resource                   | RW         | Single    | Mandatory | Double  |
 * |2   | String example resource                   | RW         | Single    | Mandatory | String  |
 * |3   | Light on                                  | RW         | Single    | Mandatory | Boolean |
 * |4   | Opaque example resource                   | RW         | Single    | Mandatory | Opaque  |
 * |5   | Firmwire update resource                  | E          | Single    | Mandatory |         |
 * |6   | Link example resource                     | RW         | Single    | Mandatory | ObjInk  |
 * |7   | Not mandatory integer example resource    | RW         | Single    | Optional  | Integer |
 * |8   | Multiple string example resource          | RW         | Multiple  | Mandatory | String  |
 * |9   | Readonly string example resource          | R          | Single    | Mandatory | String  |
 * +----+-------------------------------------------+------------+-----------+-----------+---------+
 */
@Lwm2mObjectInstance(objectId = 123)
public class ExampleMqttInstanceProxy extends ObjectInstanceProxy {

    @Lwm2mResource(id = 0)
    public ObjectResourceProxy<IntegerResourceValue> batteryLevel;

    @Lwm2mResource(id = 1)
    public ObjectResourceProxy<DoubleResourceValue> doubleExampleResource;

    @Lwm2mResource(id = 2)
    public ObjectResourceProxy<StringResourceValue> stringExampleResource;

    @Lwm2mResource(id = 3)
    public ObjectResourceProxy<BooleanResourceValue> lightOn;

    @Lwm2mResource(id = 4)
    public ObjectResourceProxy<OpaqueResourceValue> opaqueExampleResource;

    @Lwm2mResource(id = 5) // TODO without type
    public ObjectResourceProxy<BooleanResourceValue> firmwireUpdateResource;

    @Lwm2mResource(id = 6) // TODO link type
    public ObjectResourceProxy<StringResourceValue> linkExampleResource;

    @Lwm2mResource(id = 7)
    public ObjectResourceProxy<IntegerResourceValue> optionalIntegerResource;

    @Lwm2mResource(id = 8)
    public ObjectMultipleResourceProxy<StringResourceValue> multipleStringExample;

    @Lwm2mResource(id = 9)
    public ObjectMultipleResourceProxy<StringResourceValue> multipleOptionalStringExample;


    public ExampleMqttInstanceProxy() {
    }

    public ExampleMqttInstanceProxy(int id) {
        super(id);
    }
}