package performance;

import com.agh.fastmachine.core.api.model.Operations;
import com.agh.fastmachine.core.api.model.annotation.Lwm2mObject;
import com.agh.fastmachine.core.api.model.annotation.Lwm2mObjectInstance;
import com.agh.fastmachine.core.api.model.annotation.Lwm2mResource;
import com.agh.fastmachine.core.api.model.resourcevalue.IntegerResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.StringResourceValue;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;

import static com.agh.fastmachine.core.api.model.Operations.READ;

@Lwm2mObjectInstance(objectId = 20004)
public class TestInstanceProxy extends ObjectInstanceProxy {

    @Lwm2mResource(id = 0, permissions = READ)
    public StringResourceValue clientId;

    @Lwm2mResource(id = 1, permissions = READ)
    public StringResourceValue serverId;

    @Lwm2mResource(id = 2, permissions = READ)
    public StringResourceValue payload;
}
