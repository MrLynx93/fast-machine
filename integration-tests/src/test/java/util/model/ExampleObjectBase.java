package util.model;


import com.agh.fastmachine.client.api.model.ObjectBase;
import com.agh.fastmachine.core.api.model.annotation.Lwm2mObject;

import java.util.Arrays;
import java.util.List;

@Lwm2mObject(id = 1789)
public class ExampleObjectBase extends ObjectBase<ExampleObjectInstance> {

    public ExampleObjectBase() {
        super(ExampleObjectInstance.class);
    }

}
