package com.agh.fastmachine.client.internal.service.registration;

final class ServerInfo {
    public String serverRegisterEndpoint;
    public Integer lifetime;

    public ServerInfo(String serverRegisterEndpoint, Integer lifetime) {
        this.lifetime = lifetime;
        this.serverRegisterEndpoint = serverRegisterEndpoint;
    }
}