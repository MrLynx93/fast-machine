package com.agh.fastmachine.client.api.model.builtin;

import com.agh.fastmachine.client.internal.message.request.Lwm2mContentRequest;
import com.agh.fastmachine.client.internal.message.request.Lwm2mRequest;
import com.agh.fastmachine.client.internal.message.response.Lwm2mCreateResponse;
import com.agh.fastmachine.client.internal.message.response.Lwm2mResponse;
import com.agh.fastmachine.client.api.model.ObjectBase;
import com.agh.fastmachine.core.api.model.annotation.Lwm2mObject;
import org.eclipse.californium.core.coap.CoAP;

@Lwm2mObject(id = 1)
public class ServerObjectBase extends ObjectBase<ServerObjectInstance> {

    public ServerObjectBase() {
        super(ServerObjectInstance.class);
    }

    @Override
    public Lwm2mResponse handleDelete(Lwm2mRequest request) {
        ServerObjectInstance toDelete = objectInstances.get(Integer.parseInt(request.getPath().get(1)));
        Lwm2mResponse response = super.handleDelete(request);
        if (response.code == CoAP.ResponseCode.DELETED) {
            SecurityObjectInstance so = client.getServerSecurityMap().remove(toDelete.getId());
            //TODO one should remove securityObject or not?
            client.getServerObjectResolver().removeServerObject(toDelete);
        }
        return response;
    }

    @Override
    public Lwm2mCreateResponse<ServerObjectInstance> handleCreate(Lwm2mContentRequest request, Integer requestedId) {
        Lwm2mCreateResponse<ServerObjectInstance> response = super.handleCreate(request, null);
        if (response.code == CoAP.ResponseCode.CREATED) {
            client.getRegistrationInterface().registerIntoServer(response.getInstance());
        }
        return response;
    }
}
