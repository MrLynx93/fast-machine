package com.test.model.test;

import com.agh.fastmachine.client.api.model.ObjectBase;
import com.agh.fastmachine.core.api.model.annotation.Lwm2mObject;


@Lwm2mObject(id = 20004)
public class TestObjectBase extends ObjectBase<TestObjectInstance> {

    public TestObjectBase() {
        super(TestObjectInstance.class);
    }

}