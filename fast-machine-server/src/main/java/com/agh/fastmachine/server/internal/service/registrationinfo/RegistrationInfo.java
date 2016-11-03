package com.agh.fastmachine.server.internal.service.registrationinfo;

import java.util.List;

public final class RegistrationInfo {
    public String endpointClientName;
    public Integer lifetime;
    public String lwm2mVersion;
    public String smsNumber;
    public BindingMode bindingMode;
    public List<RegistrationObjectInfo> objects;

    public String url;
    public String resourceType;
    public Integer contentType;
}
