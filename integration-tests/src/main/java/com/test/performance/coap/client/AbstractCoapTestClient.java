package com.test.performance.coap.client;

import com.agh.fastmachine.client.api.Client;
import com.agh.fastmachine.client.api.model.ObjectBase;
import com.agh.fastmachine.client.api.model.builtin.SecurityObjectBase;
import com.agh.fastmachine.client.api.model.builtin.SecurityObjectInstance;
import com.agh.fastmachine.client.api.model.builtin.ServerObjectBase;
import com.agh.fastmachine.client.api.model.builtin.ServerObjectInstance;
import com.agh.fastmachine.core.api.model.resourcevalue.BooleanResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.IntegerResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.StringResourceValue;
import com.test.performance.AbstractTest;
import com.test.performance.model.TestObjectBase;
import com.test.performance.model.TestObjectInstance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public abstract class AbstractCoapTestClient extends AbstractTest {
    private CountDownLatch counter = new CountDownLatch(TIMES * SERVERS_NUMBER * CLIENTS_NUMBER);
    private List<Client> clients;

    public void test() throws InterruptedException, IOException {
        clients = new ArrayList<>();
        for (int i = 0; i < CLIENTS_NUMBER; i++) {
            Client client = configureClient(i + 1);
            clients.add(client);
        }
        clients.forEach(Client::start);

        /* Wait until all operation finished */
        System.out.println("Started all clients");
        executor.submit(() -> {
            try {
                counter.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("Started deregistering");
            clients.forEach(Client::stop);

            System.out.println("Finished deregistering");
            System.exit(0);
        });

        System.in.read();
        System.out.println("Canceled test");
        System.out.println("Started deregistering");
        clients.forEach(Client::stop);

        System.out.println("Finished deregistering");
        System.exit(0);
    }

    abstract Client configureClient(int i);

    List<ObjectBase<?>> factoryBootstrap(int clientId) {
        ServerObjectBase serverBase = new ServerObjectBase();
        SecurityObjectBase securityBase = new SecurityObjectBase();
        TestObjectBase testBase = new TestObjectBase();

        for (int serverId = 1; serverId <= SERVERS_NUMBER; serverId++) {
            ServerObjectInstance serverInst = serverBase.getNewInstance(serverId);
            serverInst.shortServerId.setValue(new IntegerResourceValue(serverId));
            serverInst.lifetime.setValue(new IntegerResourceValue(LIFETIME));
            serverInst.binding.setValue(new StringResourceValue("U"));

            SecurityObjectInstance securityInst = securityBase.getNewInstance(serverId);
            securityInst.shortServerId.setValue(new IntegerResourceValue(serverId));
            securityInst.bootstrapServer.setValue(new BooleanResourceValue(false));
            securityInst.serverUri.setValue(new StringResourceValue(SERVER_URL + (19000 + serverId)));
        }

        TestObjectInstance testInstance = testBase.getNewInstance(0);
        testInstance.clientId.setValue(new StringResourceValue("client_" + (29000 + clientId)));
        testInstance.serverId.setValue(new StringResourceValue("global"));
        testInstance.payload.setValue(new StringResourceValue("ABC"));
        testInstance.setCounter(counter);

        return Arrays.asList(serverBase, securityBase, testBase);
    }
}
