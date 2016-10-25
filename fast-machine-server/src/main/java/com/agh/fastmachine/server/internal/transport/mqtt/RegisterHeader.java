package com.agh.fastmachine.server.internal.transport.mqtt;

import com.agh.fastmachine.server.internal.service.registrationinfo.BindingMode;
import lombok.Data;

@Data
public class RegisterHeader {
    private String header;
    private String endpointClientName;
    private Integer lifetime;
    private String version;
    private BindingMode bindingMode;
    private String smsNumber;

    public static RegisterHeader fromString(String header) {
        RegisterHeader registerHeader = new RegisterHeader();
        registerHeader.header = header;

        String[] params = header.split("&");
        for (String param : params) {
            String[] elements = param.split("=");
            if ("lt".equals(elements[0])) {
                registerHeader.lifetime = Integer.parseInt(elements[1]);
            }
            if ("sms".equals(elements[0])) {
                registerHeader.smsNumber = elements[1];
            }
            if ("b".equals(elements[0])) {
                registerHeader.bindingMode = BindingMode.parseFromString(elements[1]);
            }
            if ("lwm2m".equals(elements[0])) {
                registerHeader.version = elements[1];
            }
            if ("ep".equals(elements[0])) {
                registerHeader.endpointClientName = elements[1];
            }
        }
        return registerHeader;
    }

    public byte[] toBytes() {
        return new byte[0]; // TODO
    }
}
