package com.agh.fastmachine.server.internal.transport;

import com.agh.fastmachine.server.internal.transport.LWM2M;
import lombok.AllArgsConstructor;
import lombok.Data;

import static com.agh.fastmachine.server.internal.transport.LWM2M.ContentType.PLAIN_TEXT;

@Data
@AllArgsConstructor
public abstract class Lwm2mRequest {
    protected final LWM2M.ContentType contentType;
    protected final Boolean observeFlag;
    protected final byte[] payload;

    public abstract String getToken();
    public abstract LWM2M.Operation getOperation();

    public String getPayloadText() {
        if (contentType != PLAIN_TEXT) {
            throw new UnsupportedOperationException("Message is not of type (text/plain)");
        }
        return new String(payload);
    }

}
