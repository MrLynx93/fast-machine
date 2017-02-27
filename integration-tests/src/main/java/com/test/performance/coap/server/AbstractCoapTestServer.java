package com.test.performance.coap.server;

import com.agh.fastmachine.server.api.Server;
import com.test.performance.AbstractTest;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCoapTestServer extends AbstractTest {

    public void test() throws InterruptedException {
        List<Server> servers = new ArrayList<>();

        for (int i = 0; i < SERVERS_NUMBER; i++) {
            Server server = configureServer(i + 1);
            prepareTest(server);
            servers.add(server);
        }
        servers.forEach(Server::start);
        System.out.println("You can now run all clients");

        /* Wait until all clients/servers deregistered */
        deregisteredCount.await();
        servers.forEach(server -> server.getStats().logStats());
        servers.forEach(Server::stop);
        System.exit(0);
    }

    abstract Server configureServer(int i);

}
