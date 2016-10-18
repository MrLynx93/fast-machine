package com.agh.fastmachine.server.internal.transport.mqtt.message;

//public enum ResponseCode {
//    CREATED(201),
//    DELETED(202),
//    CHANGED(204),
//    CONTENT(205),
//    BAD_REQUEST(400),
//    UNAUTHORIZED(401),
//    FORBIDDEN(403),
//    NOT_FOUND(404),
//    METHOD_NOT_ALLOWED(405),
//    UNSUPPORTED_CONTENT_FORMAT(415);
//
//    private int code;
//
//    ResponseCode(int code) {
//        this.code = code;
//    }
//
//    public boolean isSuccess() {
//        return
//    }
//
//    public static ResponseCode from(byte code) {
//        int value = (code & 0b10000000) * 100;
//        value += code & (0b01111111);
//
//        ResponseCode[] values = ResponseCode.values();
//        for (ResponseCode responseCode : values) {
//            if (responseCode.code == value) {
//                return responseCode;
//            }
//        }
//        return null;
//    }
//
//    public byte toByte() {
//        byte value = (byte) (code % 100);
//        if (code / 100 == 4) {
//            value &= 0b10000000;
//        }
//        return value;
//    }
//}
