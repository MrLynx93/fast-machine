package com.agh.fastmachine.server.api;


import com.agh.fastmachine.core.api.model.annotation.Lwm2mObjectInstance;
import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.builtin.AccessControlObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.builtin.SecurityObjectInstanceProxy;
import com.agh.fastmachine.server.api.model.builtin.ServerObjectInstanceProxy;

import java.util.HashMap;
import java.util.Map;

public class ServerConfiguration {
    public static final int TRASPORT_COAP = 0;
    public static final int TRASPORT_MQTT = 1;
    private Map<Integer, Class<? extends ObjectInstanceProxy>> instanceClasses = new HashMap<>();
    private Integer port;
    private String name;
    private int transport = TRASPORT_COAP;

    public ServerConfiguration() {
        addObjectSupport(AccessControlObjectInstanceProxy.class);
        addObjectSupport(SecurityObjectInstanceProxy.class);
        addObjectSupport(ServerObjectInstanceProxy.class);
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void addObjectSupport(Class<? extends ObjectInstanceProxy> instanceClass) {
        instanceClasses.put(instanceClass.getAnnotation(Lwm2mObjectInstance.class).objectId(), instanceClass);
    }

    public Integer getPort() {
        return port;
    }

    public Map<Integer, Class<? extends ObjectInstanceProxy>> getSupportedObjects() {
        return instanceClasses;
    }

    public int getTransport() {
        return transport;
    }

    public void setTransport (int transport) {
        this.transport = transport;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
