package com.agh.fastmachine.server.internal.transport.coap;

import com.agh.fastmachine.server.internal.transport.LWM2M;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

final class COAP {
    private COAP() {}

    static CoAP.Code getCoapMethod(LWM2M.Operation operation, LWM2M.Path path) {
        switch (operation) {
            case BS_REQ:
            case BS_FINISH:
            case R_REGISTER:
            case R_UPDATE:
            case M_EXECUTE:
            case M_CREATE:
                return CoAP.Code.POST;
            case BS_WRITE:
            case M_WRITE_ATTRIBUTE:
                return CoAP.Code.PUT;
            case BS_DELETE:
            case R_DEREGISTER:
            case M_DELETE:
                return CoAP.Code.DELETE;
            case M_READ:
            case M_DISCOVER:
            case I_OBSERVE:
            case I_CANCEL_OBSERVATION:
            case I_NOTIFY:
                return CoAP.Code.GET;
            case M_WRITE:
                return path.getResourceId() != null ? CoAP.Code.PUT : CoAP.Code.POST;
            default:
                throw new UnsupportedOperationException("Operation " + operation + " is not defined");
        }
    }

    static int getContentTypeCode(LWM2M.ContentType contentType) {
        switch (contentType) {
            case NO_FORMAT:   return MediaTypeRegistry.UNDEFINED;
            case PLAIN_TEXT:  return MediaTypeRegistry.TEXT_PLAIN;
            case LINK_FORMAT: return MediaTypeRegistry.APPLICATION_LINK_FORMAT;
            case OPAQUE:      return MediaTypeRegistry.APPLICATION_OCTET_STREAM;
            case TLV:         return 1544;
            default: throw new UnsupportedOperationException("Content-Type " + contentType + " is not defined");
        }
    }

    static LWM2M.ContentType getContentType(int contentTypeCode) {
        switch (contentTypeCode) {
            case MediaTypeRegistry.UNDEFINED:                return LWM2M.ContentType.NO_FORMAT;
            case MediaTypeRegistry.TEXT_PLAIN:               return LWM2M.ContentType.PLAIN_TEXT;
            case MediaTypeRegistry.APPLICATION_LINK_FORMAT:  return LWM2M.ContentType.LINK_FORMAT;
            case MediaTypeRegistry.APPLICATION_OCTET_STREAM: return LWM2M.ContentType.OPAQUE;
            case 1544:                                       return LWM2M.ContentType.TLV;
            default: throw new UnsupportedOperationException("Content-Type " + contentTypeCode + " is not defined");
        }
    }

    static LWM2M.ResponseCode getResponseCode(CoAP.ResponseCode coapResponseCode) {
        switch (coapResponseCode) {
            case CREATED:                    return LWM2M.ResponseCode.CREATED;
            case DELETED:                    return LWM2M.ResponseCode.DELETED;
            case CHANGED:                    return LWM2M.ResponseCode.CHANGED;
            case CONTENT:                    return LWM2M.ResponseCode.CONTENT;
            case BAD_REQUEST:                return LWM2M.ResponseCode.BAD_REQUEST;
            case UNAUTHORIZED:               return LWM2M.ResponseCode.UNAUTHORIZED;
            case FORBIDDEN:                  return LWM2M.ResponseCode.FORBIDDEN;
            case NOT_FOUND:                  return LWM2M.ResponseCode.NOT_FOUND;
            case METHOD_NOT_ALLOWED:         return LWM2M.ResponseCode.METHOD_NOT_ALLOWED;
            case UNSUPPORTED_CONTENT_FORMAT: return LWM2M.ResponseCode.UNSUPPORTED_CONTENT_FORMAT;
            default: throw new UnsupportedOperationException("Response-Code " + coapResponseCode + " is not defined");
        }
    }

}
