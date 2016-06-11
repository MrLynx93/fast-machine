package com.agh.fastmachine.client.api.model.builtin;

import com.agh.fastmachine.client.internal.message.request.Lwm2mContentRequest;
import com.agh.fastmachine.client.internal.message.response.Lwm2mResponse;
import com.agh.fastmachine.client.api.model.ObjectInstance;
import com.agh.fastmachine.client.api.model.ObjectMultipleResource;
import com.agh.fastmachine.client.api.model.ObjectResource;
import com.agh.fastmachine.core.api.model.annotation.Lwm2mResource;
import com.agh.fastmachine.core.api.model.annotation.Mandatory;
import com.agh.fastmachine.core.api.model.resourcevalue.IntegerResourceValue;
import com.agh.fastmachine.core.api.model.Operations;
import org.eclipse.californium.core.coap.CoAP;

import static com.agh.fastmachine.core.api.model.Operations.*;

public class AccessControlObjectInstance extends ObjectInstance {

    @Mandatory
    @Lwm2mResource(id = 0, permissions = READ)
    public final ObjectResource<IntegerResourceValue> objectId = new ObjectResource<>();

    @Mandatory
    @Lwm2mResource(id = 1, permissions = READ)
    public final ObjectResource<IntegerResourceValue> instanceId = new ObjectResource<>();

    @Lwm2mResource(id = 2, permissions = READ | WRITE)
    public final ObjectResource<IntegerResourceValue> accessControlOwner = new ObjectResource<>();

    @Mandatory
    @Lwm2mResource(id = 3, permissions = READ | WRITE)
    public final ObjectMultipleResource<IntegerResourceValue> accessControlList = new AccessControlOwnerResorce();

    public AccessControlObjectInstance(int id) {
        super(id);
    }

    private class AccessControlOwnerResorce extends ObjectMultipleResource<IntegerResourceValue> {

        @Override
        public Lwm2mResponse handleWrite(Lwm2mContentRequest request) {
            Lwm2mResponse response = super.handleWrite(request);
            if (response.code == CoAP.ResponseCode.CHANGED) {
                getAccessControlObjectInstance().accessControlOwner.setValue(accessControlOwner.getValue());
            }
            return response;
        }
    }
}
