package com.agh.fastmachine.client.api.model.builtin;

import com.agh.fastmachine.client.internal.message.request.Lwm2mContentRequest;
import com.agh.fastmachine.client.internal.message.request.Lwm2mRequest;
import com.agh.fastmachine.client.internal.message.response.Lwm2mCreateResponse;
import com.agh.fastmachine.client.internal.message.response.Lwm2mResponse;
import com.agh.fastmachine.client.api.model.ObjectBase;
import com.agh.fastmachine.core.api.model.annotation.Lwm2mObject;
import com.agh.fastmachine.core.api.model.annotation.SingleInstance;
import org.eclipse.californium.core.coap.CoAP;

import java.util.Arrays;
import java.util.List;

@SingleInstance
@Lwm2mObject(id = 2)
public class AccessControlObjectBase extends ObjectBase<AccessControlObjectInstance> {

    public AccessControlObjectBase() {
        super(AccessControlObjectInstance.class);
    }

    @Override
    public Lwm2mResponse handleDelete(Lwm2mRequest request) {
        return new Lwm2mResponse(CoAP.ResponseCode.BAD_REQUEST);
    }

    @Override
    public Lwm2mCreateResponse<AccessControlObjectInstance> handleCreate(Lwm2mContentRequest request) {
        return new Lwm2mCreateResponse<>(CoAP.ResponseCode.BAD_REQUEST, "Cannot create AccessControlObject manually");
    }

}
