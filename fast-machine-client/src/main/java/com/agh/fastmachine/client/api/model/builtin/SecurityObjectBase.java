package com.agh.fastmachine.client.api.model.builtin;

import com.agh.fastmachine.client.api.model.ObjectBase;
import com.agh.fastmachine.core.api.model.annotation.Lwm2mObject;

import java.util.Arrays;
import java.util.List;

@Lwm2mObject(id = 0)
public class SecurityObjectBase extends ObjectBase<SecurityObjectInstance> {

    public SecurityObjectBase() {
        super(SecurityObjectInstance.class);
    }

}
