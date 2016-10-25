package com.agh.fastmachine.server.api;

import com.agh.fastmachine.server.api.model.ObjectInstanceProxy;
import com.agh.fastmachine.server.internal.client.ClientProxyStatus;
import com.agh.fastmachine.server.api.model.ObjectTree;

public interface ClientProxy {
//    public void bootstrap(String clientUrl);
    public String getClientId();
    public String getServerId();
    public String getClientEndpointName();
    public ObjectTree getObjectTree();
    public ClientProxyStatus getStatus();
    public void waitForRegistration();
    public <T extends ObjectInstanceProxy> void create(T patternInstance);
    public <T extends ObjectInstanceProxy> void create(T patternInstance, int id);
}
