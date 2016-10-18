package com.agh.fastmachine.server.internal.transport.coap;

import com.agh.fastmachine.server.internal.transport.LWM2M;
import com.agh.fastmachine.server.internal.transport.Lwm2mResponse;
import lombok.Data;
import org.eclipse.californium.core.CoapResponse;

@Data
class Lwm2mCoapResponse extends Lwm2mResponse {
    private final Integer createdInstanceId;
    private final String token;

    private Lwm2mCoapResponse(LWM2M.ContentType contentType, LWM2M.ResponseCode responseCode, byte[] payload, Integer createdInstanceId, String token) {
        super(contentType, responseCode, payload);
        this.createdInstanceId = createdInstanceId;
        this.token = token;
    }

    public static Lwm2mCoapResponse fromCoapResponse(CoapResponse coapResponse) {
        return new Lwm2mCoapResponse(
                COAP.getContentType(coapResponse.getOptions().getContentFormat()),
                COAP.getResponseCode(coapResponse.getCode()),
                coapResponse.getPayload(),
                Integer.parseInt(coapResponse.getOptions().getLocationPath().get(1)),
                coapResponse.advanced().getTokenString()
        );
    }
}
