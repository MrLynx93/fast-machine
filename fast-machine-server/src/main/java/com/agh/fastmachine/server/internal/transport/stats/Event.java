package com.agh.fastmachine.server.internal.transport.stats;

import com.agh.fastmachine.server.internal.transport.LWM2M;
import lombok.Data;

@Data
public class Event {
    private String direction; // uplink|downlink
    private String type;      // req|res
    private boolean success;   // false if timeout
    private LWM2M.Operation op;

    public Event(String direction, String type, boolean success, LWM2M.Operation op) {
        this.direction = direction;
        this.type = type;
        this.success = success;
        this.op = op;
    }

    // All uplink

    public static Event uplinkRequestReceiveSuccess(LWM2M.Operation op) {
        return new Event("uplink  ", "req", true, op);
    }

    public static Event uplinkRequestReceiveTimeout(LWM2M.Operation op) {
        return new Event("uplink  ", "req", false, op);
    }

    public static Event uplinkResponseSendSuccess(LWM2M.Operation op) {
        return new Event("uplink  ", "res", true, op);
    }

    public static Event uplinkResponseSendTimeout(LWM2M.Operation op) {
        return new Event("uplink  ", "res", false, op);
    }

    // All downlink

    public static Event downlinkRequestSendSuccess(LWM2M.Operation op) {
        return new Event("downlink", "req", true, op);
    }

    public static Event downlinkRequestSendTimeout(LWM2M.Operation op) {
        return new Event("downlink", "req", false, op);
    }

    public static Event downlinkResponseReceiveSuccess(LWM2M.Operation op) {
        return new Event("downlink", "res", true, op);
    }

    public static Event downlinkResponseReceiveTimeout(LWM2M.Operation op) {
        return new Event("downlink", "res", false, op);
    }

}
