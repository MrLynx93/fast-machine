package com.agh.fastmachine.client.internal;

import com.agh.fastmachine.client.api.model.builtin.ServerObjectInstance;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ServerObjectResolver {
    private Map<SocketAddress, ServerObjectInstance> servers = new HashMap<>();
    private Map<Integer, ServerObjectInstance> serversByShortServerId = new HashMap<>();

    public ServerObjectInstance resolveServerObject(CoapExchange exchange) {
        System.out.println("AAAAAAAAAAAAAAAAA");
        for (SocketAddress socketAddress : servers.keySet()) {
            System.out.println("Addr " + socketAddress);
        }
        System.out.println("BBBBBBBBBBBBBBBB");
        System.out.println(new InetSocketAddress(exchange.getSourceAddress(), exchange.getSourcePort()));

        return servers.get(new InetSocketAddress(exchange.getSourceAddress(), exchange.getSourcePort()));
    }

    public ServerObjectInstance resolveServerObject(int shortServerId) {
        return serversByShortServerId.get(shortServerId);
    }

    public List<ServerObjectInstance> getRegisteredServers() {
        return new ArrayList<>(servers.values());
    }

    public int getNumberOfRegisteredServers() {
        return servers.size();
    }

    public void addServerObjectInstance(SocketAddress address, ServerObjectInstance object) {
        serversByShortServerId.put(object.getShortServerId(), object);
        servers.put(address, object);
    }

    public void removeServerObject(ServerObjectInstance instance) {
        serversByShortServerId.values().remove(instance);
        servers.values().remove(instance);
    }
}
