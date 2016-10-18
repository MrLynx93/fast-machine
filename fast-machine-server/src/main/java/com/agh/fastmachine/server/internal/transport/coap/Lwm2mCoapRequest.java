package com.agh.fastmachine.server.internal.transport.coap;

import com.agh.fastmachine.server.internal.transport.LWM2M;
import com.agh.fastmachine.server.internal.transport.Lwm2mRequest;
import lombok.Data;
import org.eclipse.californium.core.coap.Request;

@Data
class Lwm2mCoapRequest extends Lwm2mRequest {
    private final String token;
    private final LWM2M.Path path;
    private final LWM2M.Operation operation;

    private Lwm2mCoapRequest(LWM2M.ContentType contentType, Boolean observeFlag, byte[] payload, String token, LWM2M.Path path, LWM2M.Operation operation) {
        super(contentType, observeFlag, payload);
        this.token = token;
        this.path = path;
        this.operation = operation;
    }

    public static Lwm2mCoapRequest of(LWM2M.Path path, LWM2M.Operation operation, String token, LWM2M.ContentType contentType, byte[] payload, Boolean observeFlag) {

    }

    public static Lwm2mCoapRequest of(LWM2M.Path path, LWM2M.Operation operation, String token, LWM2M.ContentType contentType, byte[] payload) {

    }


    public static Lwm2mCoapRequest of(LWM2M.Path path, LWM2M.Operation operation, String token, LWM2M.ContentType contentType) {

    }

    public static Lwm2mCoapRequest of(LWM2M.Path path, LWM2M.Operation operation, String token) {

    }

    public Request toCoapRequest() {
        Request coapRequest = new Request(COAP.getCoapMethod(operation, path));
        coapRequest.getOptions().setContentFormat(COAP.getContentTypeCode(contentType));
        coapRequest.setToken(token.getBytes());

        if (operation == LWM2M.Operation.I_OBSERVE || operation == LWM2M.Operation.M_READ && observeFlag) {
            coapRequest.setObserve();
        }
        if (operation == LWM2M.Operation.I_CANCEL_OBSERVATION) {
            coapRequest.setObserveCancel();
        }
        return coapRequest;
    }

}
