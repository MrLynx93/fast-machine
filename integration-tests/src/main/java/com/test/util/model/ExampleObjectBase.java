package com.test.util.model;


import com.agh.fastmachine.client.api.model.ObjectBase;
import com.agh.fastmachine.core.api.model.annotation.Lwm2mObject;

@Lwm2mObject(id = 1789)
public class ExampleObjectBase extends ObjectBase<ExampleObjectInstance> {

    public ExampleObjectBase() {
        super(ExampleObjectInstance.class);
    }

}
