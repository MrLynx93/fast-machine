package util.model;


import com.agh.fastmachine.client.api.model.ObjectInstance;
import com.agh.fastmachine.client.api.model.ObjectMultipleResource;
import com.agh.fastmachine.client.api.model.ObjectResource;
import com.agh.fastmachine.core.api.model.annotation.Lwm2mResource;
import com.agh.fastmachine.core.api.model.resourcevalue.DateResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.FloatResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.IntegerResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.StringResourceValue;

import static com.agh.fastmachine.core.api.model.Operations.ALL;

public class ExampleObjectInstance extends ObjectInstance {

    @Lwm2mResource(id = 1, permissions = ALL)
    public ObjectResource<FloatResourceValue> floatResource = new ObjectResource<>();

    @Lwm2mResource(id = 2, permissions = ALL)
    public ObjectResource<StringResourceValue> stringResource = new ObjectResource<>();

    @Lwm2mResource(id = 3, permissions = ALL)
    public ObjectResource<DateResourceValue> dateResource = new ObjectResource<>();

    @Lwm2mResource(id = 4, permissions = ALL)
    public ObjectResource<IntegerResourceValue> integerResource = new ObjectResource<>();

    @Lwm2mResource(id = 5, permissions = ALL)
    public ObjectMultipleResource<IntegerResourceValue> multipleIntegerResource = new ObjectMultipleResource<>();


    public ExampleObjectInstance(int id) {
        super(id);
    }

}
