package com.agh.fastmachine.server.internal.transport;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import static com.agh.fastmachine.server.internal.transport.LWM2M.ContentType.PLAIN_TEXT;

@Data
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Lwm2mResponse {
    protected final LWM2M.ContentType contentType;
    protected final LWM2M.ResponseCode responseCode;
    protected final byte[] payload;

    public abstract Integer getCreatedInstanceId();
    public abstract String getToken();

    public boolean isSuccess() {
        return responseCode.isSuccess();
    }

    public String getPayloadText() {
        if (contentType != PLAIN_TEXT) {
            throw new UnsupportedOperationException("Message is not of type (text/plain)");
        }
        return new String(payload);
    }

}
