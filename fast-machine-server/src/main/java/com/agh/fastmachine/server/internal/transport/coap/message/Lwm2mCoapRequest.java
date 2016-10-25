package com.agh.fastmachine.server.internal.transport.coap.message;

import com.agh.fastmachine.server.internal.transport.LWM2M;
import com.agh.fastmachine.server.internal.transport.Lwm2mRequest;
import com.agh.fastmachine.server.internal.transport.coap.COAP;
import lombok.Data;
import org.eclipse.californium.core.coap.Request;

import static com.agh.fastmachine.server.internal.transport.LWM2M.ContentType.*;
import static com.agh.fastmachine.server.internal.transport.LWM2M.Operation.*;

@Data
public class Lwm2mCoapRequest extends Lwm2mRequest {
    private final String token;
    private final LWM2M.Path path;
    private final LWM2M.Operation operation;

    private Lwm2mCoapRequest(LWM2M.Path path, LWM2M.Operation operation, String token, LWM2M.ContentType contentType, byte[] payload, Boolean observeFlag) {
        super(contentType, observeFlag, payload);
        this.path = path;
        this.operation = operation;
        this.token = token;
    }

    Lwm2mCoapRequest(LWM2M.Path path, LWM2M.Operation operation, String token, LWM2M.ContentType contentType, byte[] payload) {
        this(path, operation, token, contentType, payload, false);
    }


    Lwm2mCoapRequest(LWM2M.Path path, LWM2M.Operation operation, String token, LWM2M.ContentType contentType) {
        this(path, operation, token, contentType, null, false);
    }

    Lwm2mCoapRequest(LWM2M.Path path, LWM2M.Operation operation, String token) {
        this(path, operation, token, NO_FORMAT, null, false);
    }

    public String getCoapPath() {
        return path.toString();
    }

    public Request toCoapRequest() {
        Request coapRequest = new Request(COAP.getCoapMethod(operation, path));
        coapRequest.setToken(token.getBytes());
        coapRequest.setPayload(payload);

        int format = COAP.getContentTypeCode(contentType);
        if (format >= 0) {
            coapRequest.getOptions().setContentFormat(format);
            coapRequest.getOptions().setAccept(format);
        }

        if (operation == I_OBSERVE || operation == M_READ && observeFlag) {
            coapRequest.setObserve();
        }
        if (operation == I_CANCEL_OBSERVATION) {
            coapRequest.setObserveCancel();
        }
        return coapRequest;
    }

}
