package com.agh.fastmachine.server.internal.transport.mqtt;

import com.agh.fastmachine.server.internal.transport.LWM2M;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

public final class MQTT {
    private MQTT() {}

    @Data
    @AllArgsConstructor
    public static class Topic {
        private LWM2M.Operation operation;
        private String type;
        private String token;
        private String clientId;
        private String serverId;
        private LWM2M.Path path;

        public static Topic fromString(String topic) {
            String[] elements = topic.split("/");
            LWM2M.Path path = LWM2M.Path.of(
                    elements.length > 5 ? Integer.parseInt(elements[5]) : null,
                    elements.length > 6 ? Integer.parseInt(elements[6]) : null,
                    elements.length > 7 ? Integer.parseInt(elements[7]) : null
            );
            return new Topic(
                    getOperationFromCode(elements[0]),
                    elements[1],
                    elements[2],
                    elements[3],
                    elements[4],
                    path
            );
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("");
            builder.append(getOperationCode(operation)).append("/");
            builder.append(type).append("/");
            builder.append(token).append("/");
            builder.append(clientId).append("/");
            builder.append(serverId);
            if (path.getObjectId() != null) builder.append("/").append(path.getObjectId());
            if (path.getInstanceId() != null) builder.append("/").append(path.getInstanceId());
            if (path.getResourceId() != null) builder.append("/").append(path.getResourceId());
            return builder.toString();
        }
    }

    public static int getContentTypeCode(LWM2M.ContentType contentType) {
        switch (contentType) {
            case NO_FORMAT:   return 0;
            case PLAIN_TEXT:  return 1;
            case LINK_FORMAT: return 2;
            case OPAQUE:      return 3;
            case TLV:         return 4;
            default: throw new UnsupportedOperationException("Content-Type " + contentType + " is not defined");
        }
    }

    public static LWM2M.ContentType parseContentTypeFromCode(byte code) {
        switch (code) {
            case 0: return LWM2M.ContentType.NO_FORMAT;
            case 1: return LWM2M.ContentType.PLAIN_TEXT;
            case 2: return LWM2M.ContentType.LINK_FORMAT;
            case 3: return LWM2M.ContentType.OPAQUE;
            case 4: return LWM2M.ContentType.TLV;
            default: throw new UnsupportedOperationException("Content-Type " + code + " is not defined");
        }
    }

    public static String getOperationCode(LWM2M.Operation operation) {
        switch (operation) {
            case BS_REQ:               return "br";
            case BS_WRITE:             return "bw";
            case BS_DELETE:            return "bd";
            case BS_FINISH:            return "bf";
            case R_REGISTER:           return "rr";
            case R_UPDATE:             return "ru";
            case R_DEREGISTER:         return "rd";
            case M_READ:               return "mr";
            case M_WRITE:              return "mw";
            case M_EXECUTE:            return "me";
            case M_CREATE:             return "mc";
            case M_DELETE:             return "md";
            case M_WRITE_ATTRIBUTE:    return "ma";
            case M_DISCOVER:           return "mm";
            case I_OBSERVE:            return "io";
            case I_CANCEL_OBSERVATION: return "ic";
            case I_NOTIFY:             return "in";
            default: throw new UnsupportedOperationException("Operation " + operation + " is not defined");
        }
    }

    public static LWM2M.Operation getOperationFromCode(String code) {
        switch (code) {
            case "br": return LWM2M.Operation.BS_REQ;
            case "bw": return LWM2M.Operation.BS_WRITE;
            case "bd": return LWM2M.Operation.BS_DELETE;
            case "bf": return LWM2M.Operation.BS_FINISH;
            case "rr": return LWM2M.Operation.R_REGISTER;
            case "ru": return LWM2M.Operation.R_UPDATE;
            case "rd": return LWM2M.Operation.R_DEREGISTER;
            case "mr": return LWM2M.Operation.M_READ;
            case "mw": return LWM2M.Operation.M_WRITE;
            case "me": return LWM2M.Operation.M_EXECUTE;
            case "mc": return LWM2M.Operation.M_CREATE;
            case "md": return LWM2M.Operation.M_DELETE;
            case "ma": return LWM2M.Operation.M_WRITE_ATTRIBUTE;
            case "mm": return LWM2M.Operation.M_DISCOVER;
            case "io": return LWM2M.Operation.I_OBSERVE;
            case "ic": return LWM2M.Operation.I_CANCEL_OBSERVATION;
            case "in": return LWM2M.Operation.I_NOTIFY;
            default: throw new UnsupportedOperationException("Operation code " + code + " is not defined");
        }
    }

    public static byte getResponseCodeByte(LWM2M.ResponseCode responseCode) {
        byte responseByte = 0;
        if (responseCode.getCode() / 100 == 2) {
            responseByte &= 0b10000000;
        }
        responseByte += (byte) (responseCode.getCode() % 100);
        return responseByte;
    }

    public static LWM2M.ResponseCode parseResponseCodeFromByte(byte responseCodeByte) {
        int code = responseCodeByte & 0b01111111;
        code += (responseCodeByte & 0b10000000) > 0 ? 400 : 200;
        return LWM2M.ResponseCode.valueOf(code);
    }
}