package util.model;


import com.agh.fastmachine.core.api.model.resourcevalue.DateResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.FloatResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.IntegerResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.StringResourceValue;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.ObjectMultipleResourceProxy;
import com.agh.fastmachine.server.api.model.ObjectResourceProxy;
import com.agh.fastmachine.core.api.model.annotation.Lwm2mObjectInstance;
import com.agh.fastmachine.core.api.model.annotation.Lwm2mResource;

@Lwm2mObjectInstance(objectId = 1789)
public class ExampleObjectInstanceProxy extends ObjectInstanceProxy {

    @Lwm2mResource(id = 1)
    public ObjectResourceProxy<FloatResourceValue> floatResource;

    @Lwm2mResource(id = 2)
    public ObjectResourceProxy<StringResourceValue> stringResource;

    @Lwm2mResource(id = 3)
    public ObjectResourceProxy<DateResourceValue> dateResource;

    @Lwm2mResource(id = 4)
    public ObjectResourceProxy<IntegerResourceValue> integerResource;

    @Lwm2mResource(id = 5)
    public ObjectMultipleResourceProxy<IntegerResourceValue> multipleIntegerResource;

    public ExampleObjectInstanceProxy(int id) {
        super(id);
    }
}
