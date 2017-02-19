package com.agh.fastmachine.server.internal.transport.stats;

import com.agh.fastmachine.server.api.ClientProxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class Stats {
    private Map<ClientProxy, List<Event>> clientStats = new ConcurrentHashMap<>();

    public void addEvent(ClientProxy client, Event event) {
            if (!clientStats.containsKey(client)) {
                clientStats.put(client, new ArrayList<>());
            }
            clientStats.get(client).add(event);
    }

}
