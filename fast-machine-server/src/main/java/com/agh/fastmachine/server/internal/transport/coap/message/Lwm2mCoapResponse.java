package com.agh.fastmachine.server.internal.transport.coap.message;

import com.agh.fastmachine.server.internal.transport.LWM2M;
import com.agh.fastmachine.server.internal.transport.Lwm2mResponse;
import com.agh.fastmachine.server.internal.transport.coap.COAP;
import lombok.Builder;
import lombok.Data;
import org.eclipse.californium.core.CoapResponse;

import java.util.List;

@Data
public class Lwm2mCoapResponse extends Lwm2mResponse {
    private final Integer createdInstanceId;
    private final String token;

    private Lwm2mCoapResponse(LWM2M.ContentType contentType, LWM2M.ResponseCode responseCode, byte[] payload, Integer createdInstanceId, String token) {
        super(contentType, responseCode, payload);
        this.createdInstanceId = createdInstanceId;
        this.token = token;
    }

    public static Lwm2mCoapResponse fromCoapResponse(CoapResponse coapResponse) {
        List<String> locationPath = coapResponse.getOptions().getLocationPath();
        Integer createdInstanceId = locationPath.size() > 0 ? Integer.parseInt(locationPath.get(1)) : null;

        return new Lwm2mCoapResponse(
                COAP.getContentType(coapResponse.getOptions().getContentFormat()),
                COAP.getResponseCode(coapResponse.getCode()),
                coapResponse.getPayload(),
                createdInstanceId,
                new String(coapResponse.advanced().getToken())
        );
    }
}
