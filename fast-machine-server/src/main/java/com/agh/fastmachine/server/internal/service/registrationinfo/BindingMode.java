package com.agh.fastmachine.server.internal.service.registrationinfo;

public enum BindingMode {
    UDP("U"),
    UDP_QUEUE_MODE("UQ"),
    SMS("S"),
    SMS_QUEUE_MODE("SQ"),
    UDP_SMS("US"),
    UDP_SMS_QUEUE_MODE("UQS");

    private final String bindingString;

    BindingMode(String bindingString) {
        this.bindingString = bindingString;
    }

    public static BindingMode parseFromString(String bindingString) {
        for (BindingMode bindingMode : BindingMode.values()) {
            if (bindingMode.bindingString.equals(bindingString)) {
                return bindingMode;
            }
        }
        return null;
    }
}
