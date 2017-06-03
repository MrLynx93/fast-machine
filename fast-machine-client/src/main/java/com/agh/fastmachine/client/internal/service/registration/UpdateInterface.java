package com.agh.fastmachine.client.internal.service.registration;

import com.agh.fastmachine.client.api.Client;
import com.agh.fastmachine.client.api.model.builtin.ServerObjectInstance;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.Request;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.agh.fastmachine.client.internal.service.registration.RegistrationInterface.DEFAULT_LIFETIME;

public class UpdateInterface {
    private final Map<Integer, UpdateThread> updateThreadMap = new HashMap<>();
    private final Map<Integer, ServerInfo> serverInfoMap;
    private final Client client;

    public UpdateInterface(Map<Integer, ServerInfo> serverInfoMap, Client client) {
        this.serverInfoMap = serverInfoMap;
        this.client = client;
    }

    public void startUpdating(final ServerObjectInstance server) {
        UpdateThread updateThread = new UpdateThread(server);
        updateThread.shouldContinue = false;
        updateThreadMap.put(server.shortServerId.getValue().value, updateThread);
        updateThread.start();
    }

    public void stopUpdating(final ServerObjectInstance server) {
        Integer shortServerId = server.shortServerId.getValue().value;
        UpdateThread updateThread = updateThreadMap.get(shortServerId);
        if (updateThread != null) {
            updateThread.interrupt();
            updateThreadMap.put(shortServerId, null);
        }
    }

    //TODO execute if binding mode is U and socket changes
    public void executeUpdate(final ServerObjectInstance server) {
        update(server);
        UpdateThread updateThread = updateThreadMap.get(server.shortServerId.getValue().value);
        updateThread.shouldContinue = true;
        updateThread.interrupt();
    }

    public void update(ServerObjectInstance server) {
        Request updateRequest = new Request(CoAP.Code.PUT);
        StringBuilder uri = new StringBuilder();
        uri.append(serverInfoMap.get(server.shortServerId.getValue().value).serverRegisterEndpoint);
        if (server.lifetime.getValue().value != DEFAULT_LIFETIME) {
            uri.append("?lt=").append(server.lifetime.getValue().value);
        }
        updateRequest.setURI(uri.toString());
        updateRequest.send(client.getEndpoint());

        String formattedDate = DateFormat.getDateTimeInstance().format(new Date());
        System.out.println("Last update sent: " + formattedDate);
    }

    private class UpdateThread extends Thread { // todo should switch to ScheduledThreadPoolExecutor like in observation
        private final ServerObjectInstance server;
        public boolean shouldContinue = false;

        public UpdateThread(ServerObjectInstance server) {
            this.server = server;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    update(server);
                    sleepForLifetime();
                } catch (InterruptedException e) {
                    if(!shouldContinue) {
                        break;
                    }
                    shouldContinue = false;
                }
            }
        }

        private void sleepForLifetime() throws InterruptedException {
            Integer lifetime = serverInfoMap.get(server.shortServerId.getValue().value).lifetime;
            Thread.sleep(lifetime * 1000);
//            if (lifetime != null && lifetime > 15) {
//                Thread.sleep((lifetime - 15) * 1000); // TODO tak samo jak w lynx-lwm2m
//            }
//            if (lifetime != null && lifetime < 15) {
//                Thread.sleep((lifetime - 1) * 1000);
//            }
        }
    }

}
