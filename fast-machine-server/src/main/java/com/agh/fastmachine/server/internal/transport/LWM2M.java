package com.agh.fastmachine.server.internal.transport;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

public final class LWM2M {
    private LWM2M() {
    }

    public enum Operation {
        BS_REQ,
        BS_WRITE,
        BS_DELETE,
        BS_FINISH,
        R_REGISTER,
        R_UPDATE,
        R_DEREGISTER,
        M_READ,
        M_WRITE,
        M_EXECUTE,
        M_CREATE,
        M_DELETE,
        M_WRITE_ATTRIBUTE,
        M_DISCOVER,
        I_OBSERVE,
        I_CANCEL_OBSERVATION,
        I_NOTIFY
    }

    public enum ContentType {
        NO_FORMAT,
        PLAIN_TEXT,
        LINK_FORMAT,
        OPAQUE,
        TLV
    }

    public enum ResponseCode {
        CREATED(201),
        DELETED(202),
        CHANGED(204),
        CONTENT(205),
        BAD_REQUEST(400),
        UNAUTHORIZED(401),
        FORBIDDEN(403),
        NOT_FOUND(404),
        METHOD_NOT_ALLOWED(405),
        UNSUPPORTED_CONTENT_FORMAT(415);

        private int code;

        ResponseCode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public boolean isSuccess() {
            return this.code / 100 == 2;
        }

        public static ResponseCode valueOf(int code) {
            switch (code) {
                case 201: return CREATED;
                case 202: return DELETED;
                case 204: return CHANGED;
                case 205: return CONTENT;
                case 400: return BAD_REQUEST;
                case 401: return UNAUTHORIZED;
                case 403: return FORBIDDEN;
                case 404: return NOT_FOUND;
                case 405: return METHOD_NOT_ALLOWED;
                case 415: return UNSUPPORTED_CONTENT_FORMAT;
                default:  throw new UnsupportedOperationException("Response code " + code + " is not defined");
            }
        }
    }

    @Data
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Path {
        private Integer objectId;
        private Integer instanceId;
        private Integer resourceId;

        public static Path of(Integer objectId, Integer instanceId, Integer resourceId) {
            return new Path(objectId, instanceId, resourceId);
        }

        public static Path of(Integer objectId, Integer instanceId) {
            return new Path(objectId, instanceId, null);
        }

        public static Path of(Integer objectId) {
            return new Path(objectId, null, null);
        }

        public static Path empty() {
            return new Path(null, null, null);
        }

        public static Path fromString(String path) {
            String[] elements = path.split("/");
            return new Path(
                    elements.length > 0 ? Integer.parseInt(elements[0]) : null,
                    elements.length > 1 ? Integer.parseInt(elements[1]) : null,
                    elements.length > 2 ? Integer.parseInt(elements[2]) : null
            );
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            if (objectId != null) builder.append("/").append(objectId);
            if (instanceId != null) builder.append("/").append(instanceId);
            if (resourceId != null) builder.append("/").append(resourceId);
            return builder.toString();
        }
    }

}
