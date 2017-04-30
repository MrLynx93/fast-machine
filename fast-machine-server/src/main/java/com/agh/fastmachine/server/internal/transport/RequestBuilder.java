package com.agh.fastmachine.server.internal.transport;

import com.agh.fastmachine.core.api.model.resourcevalue.OpaqueResourceValue;
import com.agh.fastmachine.server.api.model.*;
import com.agh.fastmachine.server.internal.transport.coap.message.Lwm2mCoapRequest;

import java.security.SecureRandom;
import java.util.Random;

public abstract class RequestBuilder<REQ extends Lwm2mRequest> {
    private static final char[] CHARSET = "abcdefghijklmnopqrstuvwxyz1234567890".toCharArray();
    private static final int TOKEN_LENGTH = 8;
    private static SecureRandom secureRandom = new SecureRandom();

    public abstract REQ buildCreateRequest(ObjectInstanceProxy instance, int id);
    public abstract REQ buildCreateRequest(ObjectInstanceProxy instance);
    public abstract REQ buildDeleteRequest(ObjectInstanceProxy instance);
    public abstract REQ buildDiscoverRequest(ObjectNodeProxy node);
    public abstract REQ buildExecuteRequest(ObjectResourceProxy<?> resource, String arguments);
    public abstract REQ buildObserveRequest(ObjectNodeProxy<?> node);
    public abstract REQ buildCancelObserveRequest(ObjectNodeProxy<?> node);
    public abstract REQ buildReadRequest(ObjectBaseProxy<?> object);
    public abstract REQ buildReadRequest(ObjectInstanceProxy instance);
    public abstract REQ buildReadRequest(ObjectResourceProxy<?> resource);
    public abstract REQ buildWriteAttributesRequest(ObjectNodeProxy<?> node);
    public abstract REQ buildWriteRequest(ObjectInstanceProxy instance);
    public abstract REQ buildWriteRequest(ObjectResourceProxy resource);


    protected LWM2M.ContentType getResourceContentType(ObjectResourceProxy resource) {
        if (OpaqueResourceValue.class.equals(resource.getValueType())) {
            return LWM2M.ContentType.OPAQUE;
        }
        if (resource instanceof ObjectMultipleResourceProxy) {
            return LWM2M.ContentType.TLV;
        }
        return LWM2M.ContentType.PLAIN_TEXT;
    }

    protected LWM2M.ContentType getResourceFormat(ObjectResourceProxy resource) {
        if (OpaqueResourceValue.class.equals(resource.getValueType())) {
            return LWM2M.ContentType.OPAQUE;
        }
        if (resource instanceof ObjectMultipleResourceProxy) {
            return LWM2M.ContentType.TLV;
        }
        return LWM2M.ContentType.PLAIN_TEXT;
    }

    public synchronized String generateToken() {
        Random random = secureRandom;
        char[] result = new char[TOKEN_LENGTH];
        for (int i = 0; i < result.length; i++) {
            // picks a random index out of character set > random character
            int randomCharIndex = random.nextInt(CHARSET.length);
            result[i] = CHARSET[randomCharIndex];
        }
        return new String(result);
    }

}
