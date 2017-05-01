package com.agh.fastmachine.server.internal.transport.stats;

import com.agh.fastmachine.server.api.ClientProxy;
import com.agh.fastmachine.server.api.Server;
import com.agh.fastmachine.server.internal.transport.LWM2M;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class Stats {
    private static Logger LOG = LoggerFactory.getLogger(Stats.class);
    private Map<ClientProxy, List<Event>> clientStats = new ConcurrentHashMap<>();

    public void addBroadcastEvent(Server server, Event event) {
        server.getClients().values().forEach(client -> addEvent(client, event));
    }

    public synchronized void addEvent(ClientProxy client, Event event) {
        if (!clientStats.containsKey(client)) {
            clientStats.put(client, new ArrayList<>());
        }
        clientStats.get(client).add(event);

        if (!event.isSuccess()) {
            logStats();
        }
    }

    public synchronized void logStats() {
        LOG.info("============ STATS =============");
        for (ClientProxy client : clientStats.keySet()) {
            LOG.info("== CLIENT " + client.getClientEndpointName() + " =========");
            Map<LWM2M.Operation, Map<Boolean, Map<String, Map<String, Long>>>> eventCount = clientStats.get(client)
                    .stream()
                    .collect(Collectors.groupingBy(Event::getOp,
                             Collectors.groupingBy(Event::isSuccess,
                             Collectors.groupingBy(Event::getType,
                             Collectors.groupingBy(Event::getDirection,
                             Collectors.counting())))));

            for (LWM2M.Operation operation : eventCount.keySet()) {
                for (Boolean success : eventCount.get(operation).keySet()) {
                    for (String type : eventCount.get(operation).get(success).keySet()) {
                        for (String dir : eventCount.get(operation).get(success).get(type).keySet()) {
                            Long count = eventCount.get(operation).get(success).get(type).get(dir);
                            LOG.info((success ? "|+" : "|-") + " {} {} {} count\t -> {}", dir, type, operation, count);
                        }
                    }
                }
            }
        }
        LOG.info("==============================");
    }


}
