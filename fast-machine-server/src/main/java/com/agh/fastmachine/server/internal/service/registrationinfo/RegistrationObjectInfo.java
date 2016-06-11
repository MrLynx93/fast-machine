package com.agh.fastmachine.server.internal.service.registrationinfo;

public final class RegistrationObjectInfo {
    public String url;
    public Integer objectId;
    public Integer instanceId;

    public RegistrationObjectInfo(String url, Integer objectId, Integer instanceId) {
        this.url = url;
        this.objectId = objectId;
        this.instanceId = instanceId;
    }

}
