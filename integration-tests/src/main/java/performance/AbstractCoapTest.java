package performance;

import com.agh.fastmachine.client.api.Client;
import com.agh.fastmachine.client.api.model.ObjectBase;
import com.agh.fastmachine.client.api.model.builtin.SecurityObjectBase;
import com.agh.fastmachine.client.api.model.builtin.SecurityObjectInstance;
import com.agh.fastmachine.client.api.model.builtin.ServerObjectBase;
import com.agh.fastmachine.client.api.model.builtin.ServerObjectInstance;
import com.agh.fastmachine.core.api.model.resourcevalue.BooleanResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.IntegerResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.StringResourceValue;
import com.agh.fastmachine.server.api.Server;
import performance.model.TestObjectBase;
import performance.model.TestObjectInstance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

abstract class AbstractCoapTest extends AbstractTest {
    private CountDownLatch counter = new CountDownLatch(TIMES * SERVERS_NUMBER * CLIENTS_NUMBER);

    public void test() throws InterruptedException {
        List<Server> servers = new ArrayList<>();
        List<Client> clients = new ArrayList<>();

        for (int i = 0; i < SERVERS_NUMBER; i++) {
            Server server = configureServer(i + 1);
            prepareTest(server);
            servers.add(server);
        }
        for (int i = 0; i < CLIENTS_NUMBER; i++) {
            Client client = configureClient(i + 1, servers);
            clients.add(client);
        }
        servers.forEach(Server::start);
        clients.forEach(Client::start);

        /* Wait until all operation finished */
        counter.await();
        System.out.println("Started deregistering");
        clients.forEach(Client::stop);

        /* Wait until all clients/servers deregistered */
        deregisteredCount.await();
        servers.forEach(server -> server.getStats().logStats());
        servers.forEach(Server::stop);
        System.exit(0);
    }

    abstract Client configureClient(int i, List<Server> servers);

    abstract Server configureServer(int number);

    List<ObjectBase<?>> factoryBootstrap(int clientId, List<Server> servers) {
        ServerObjectBase serverBase = new ServerObjectBase();
        SecurityObjectBase securityBase = new SecurityObjectBase();
        TestObjectBase testBase = new TestObjectBase();

        for (Server server : servers) {
            int serverId = Integer.parseInt(server.getName().split("_")[1]) - 19000;
            ServerObjectInstance serverInst = serverBase.getNewInstance(serverId);
            serverInst.shortServerId.setValue(new IntegerResourceValue(serverId));
            serverInst.lifetime.setValue(new IntegerResourceValue(LIFETIME));
            serverInst.binding.setValue(new StringResourceValue("U"));

            SecurityObjectInstance securityInst = securityBase.getNewInstance(serverId);
            securityInst.shortServerId.setValue(new IntegerResourceValue(serverId));
            securityInst.bootstrapServer.setValue(new BooleanResourceValue(false));
            securityInst.serverUri.setValue(new StringResourceValue("coap://localhost:" + (19000 + serverId)));
        }

        TestObjectInstance testInstance = testBase.getNewInstance(0);
        testInstance.clientId.setValue(new StringResourceValue("client_" + (29000 + clientId)));
        testInstance.serverId.setValue(new StringResourceValue("global"));
        testInstance.payload.setValue(new StringResourceValue("ABC"));
        testInstance.setCounter(counter);

        return Arrays.asList(serverBase, securityBase, testBase);
    }

}
